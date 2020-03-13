package uk.gov.dwp.dataworks.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awssdk.services.emr.model.ActionOnFailure
import software.amazon.awssdk.services.emr.model.Configuration
import software.amazon.awssdk.services.emr.model.JobFlowInstancesConfig
import java.time.ZoneId

data class CreationRequest @JsonCreator constructor(
        val name: String,
        val releaseLabel: String,
        val serviceRole: String = "",
        val jobFlowRole: String = "",
        val autoScalingRole: String = "",
        val hostedZoneId: String = "",
        val customInstanceConfig: CustomInstanceConfig,
        val customEmrConfigs: List<EmrConfiguration> = emptyList(),
        val steps: List<Step>,
        val applications: List<String>)

data class EmrConfiguration @JsonCreator constructor(
        val classification: String,
        val properties: Map<String, String>
)

data class CustomInstanceConfig @JsonCreator constructor(
        val ec2SubnetId: String,
        val useSpotPricing: Boolean,
        val instanceTemplate: InstanceTemplate,
        val keepAlivePostJob: Boolean)

data class Step @JsonCreator constructor(
        val name: String,
        val actionOnFailure: ActionOnFailure,
        val jarPath: String,
        val args: List<String> = emptyList())

enum class InstanceTemplate(val fileName: String) {
    SMALL("small.json"),
    MEDIUM("medium.json"),
    LARGE("large.json");

    private lateinit var jobFlowInstancesConfig: JobFlowInstancesConfig.Builder

    fun get(): JobFlowInstancesConfig.Builder {
        if (!this::jobFlowInstancesConfig.isInitialized) {
            val fileContents = this::class.java.getResourceAsStream("/instanceConfigurations/$fileName")
                    ?: throw IllegalArgumentException("Cannot find file $fileName to create template")
            jobFlowInstancesConfig = ObjectMapper().readValue(fileContents, JobFlowInstancesConfig.serializableBuilderClass())
        }
        return jobFlowInstancesConfig
    }
}
