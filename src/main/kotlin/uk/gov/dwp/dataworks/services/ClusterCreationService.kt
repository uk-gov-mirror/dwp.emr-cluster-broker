package uk.gov.dwp.dataworks.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest
import software.amazon.awssdk.services.ec2.model.Filter
import software.amazon.awssdk.services.emr.EmrAsyncClient
import software.amazon.awssdk.services.emr.model.Application
import software.amazon.awssdk.services.emr.model.HadoopJarStepConfig
import software.amazon.awssdk.services.emr.model.JobFlowInstancesConfig
import software.amazon.awssdk.services.emr.model.MarketType.ON_DEMAND
import software.amazon.awssdk.services.emr.model.MarketType.SPOT
import software.amazon.awssdk.services.emr.model.RepoUpgradeOnBoot
import software.amazon.awssdk.services.emr.model.RunJobFlowRequest
import software.amazon.awssdk.services.emr.model.StepConfig
import software.amazon.awssdk.services.emr.model.Tag
import uk.gov.dwp.dataworks.model.CreationRequest
import uk.gov.dwp.dataworks.model.CustomInstanceConfig
import uk.gov.dwp.dataworks.model.Step
import uk.gov.dwp.dataworks.services.ConfigKey.AMI_SEARCH_PATTERN
import uk.gov.dwp.dataworks.services.ConfigKey.EMR_RELEASE_LABEL
import uk.gov.dwp.dataworks.services.ConfigKey.JOB_FLOW_ROLE_BLACKLIST
import uk.gov.dwp.dataworks.services.ConfigKey.S3_LOG_URI
import uk.gov.dwp.dataworks.services.ConfigKey.SECURITY_CONFIGURATION

@Service
class ClusterCreationService {
    @Autowired
    private lateinit var configService: ConfigurationService

    fun jobFlowRoleIsBlacklisted(role: String): Boolean {
        return configService.getListConfig(JOB_FLOW_ROLE_BLACKLIST).contains(role)
    }

    fun submitStepRequest(clusterId: String, creationRequest: CreationRequest) {
        val clusterRequest = RunJobFlowRequest.builder()
                .name(clusterId)
                .releaseLabel(configService.getStringConfig(EMR_RELEASE_LABEL))
                .customAmiId(getAmiId())
                .repoUpgradeOnBoot(RepoUpgradeOnBoot.NONE)
                .steps(formatSteps(creationRequest.steps))
                .logUri(configService.getStringConfig(S3_LOG_URI))
                .serviceRole(creationRequest.serviceRole)
                .jobFlowRole(creationRequest.jobFlowRole)
                .securityConfiguration(configService.getStringConfig(SECURITY_CONFIGURATION))
                .applications(creationRequest.applications.map { Application.builder().name(it).build() })
                .instances(formatInstanceConfig(creationRequest.customInstanceConfig))
                .tags(Tag.builder().key("createdBy").value("clusterBroker").build())
                .build()

        EmrAsyncClient.builder().region(configService.awsRegion).build().runJobFlow(clusterRequest)
    }

    fun getAmiId(): String {
        val images = Ec2Client.builder().region(configService.awsRegion).build()
                .describeImages(
                        DescribeImagesRequest.builder()
                                .filters(Filter.builder()
                                        .name("name")
                                        .values(configService.getStringConfig(AMI_SEARCH_PATTERN)).build())
                                .build())
                .images()
                .toMutableList()
        images.sortByDescending { it.creationDate() }
        return images.first().imageId()
    }

    fun formatSteps(steps: Iterable<Step>): List<StepConfig> {
        return steps.map {
            StepConfig.builder()
                    .name(it.name)
                    .actionOnFailure(it.actionOnFailure)
                    .hadoopJarStep(HadoopJarStepConfig.builder().jar(it.jarPath).build())
                    .build()
        }
    }

    fun formatInstanceConfig(customInstanceConfig: CustomInstanceConfig): JobFlowInstancesConfig {
        val instancesConfigBuilder = customInstanceConfig.instanceTemplate.get()

        val instanceGroups = instancesConfigBuilder.build().instanceGroups()
                .map { it.toBuilder() }
                .map { it.market(if (customInstanceConfig.useSpotPricing) SPOT else ON_DEMAND).build() }

        return instancesConfigBuilder
                .instanceGroups(instanceGroups)
                .keepJobFlowAliveWhenNoSteps(customInstanceConfig.keepAlivePostJob)
                .build()
    }
}
