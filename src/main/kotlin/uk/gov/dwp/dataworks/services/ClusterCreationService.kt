package uk.gov.dwp.dataworks.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.regions.Region
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

@Service
class ClusterCreationService(private val emrClient: EmrAsyncClient = EmrAsyncClient.builder().region(Region.EU_WEST_1).build()) {

    @Value("\${clusterBroker.amiSearchPattern}")
    private lateinit var amiSearchPattern: String

    @Value("\${clusterBroker.emrReleaseLabel}")
    private lateinit var emrReleaseLabel: String

    @Value("\${clusterBroker.s3LogUri}")
    private lateinit var s3LogUri: String

    @Value("\${clusterBroker.securityConfiguration}")
    private lateinit var securityConfiguration: String

    @Value("\${clusterBroker.jobFlowRoleBlacklist}")
    private lateinit var jobFlowRoleBlacklist: List<String>

    fun jobFlowRoleIsBlacklisted(role: String): Boolean {
        return jobFlowRoleBlacklist.contains(role)
    }

    fun submitStepRequest(clusterId: String, creationRequest: CreationRequest) {
        val clusterRequest = RunJobFlowRequest.builder()
                .name(clusterId)
                .releaseLabel(emrReleaseLabel)
                .customAmiId(getAmiId())
                .repoUpgradeOnBoot(RepoUpgradeOnBoot.NONE)
                .steps(formatSteps(creationRequest.steps))
                .logUri(s3LogUri)
                .serviceRole(creationRequest.serviceRole)
                .jobFlowRole(creationRequest.jobFlowRole)
                .securityConfiguration(securityConfiguration)
                .applications(creationRequest.applications.map { Application.builder().name(it).build() })
                .instances(formatInstanceConfig(creationRequest.customInstanceConfig))
                .tags(Tag.builder().key("createdBy").value("clusterBroker").build())
                .build()

        emrClient.runJobFlow(clusterRequest)
    }

    fun getAmiId(): String {
        val images = Ec2Client.builder().region(Region.EU_WEST_2).build()
                .describeImages(
                        DescribeImagesRequest.builder()
                                .filters(Filter.builder()
                                        .name("name")
                                        .values(amiSearchPattern).build())
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
