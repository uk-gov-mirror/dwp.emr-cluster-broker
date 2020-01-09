package uk.gov.dwp.dataworks.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awssdk.services.emr.model.ActionOnFailure
import software.amazon.awssdk.services.emr.model.JobFlowInstancesConfig
import java.lang.IllegalArgumentException

data class CreationRequest @JsonCreator constructor(
        val name: String,
        val releaseLabel: String,
        val serviceRole: String,
        val jobFlowRole: String,
        val customInstanceConfig: CustomInstanceConfig,
        val steps: List<Step>,
        val applications: List<String>)

data class CustomInstanceConfig @JsonCreator constructor(
        val useSpotPricing: Boolean,
        val instanceTemplate: InstanceTemplate,
        val keepAlivePostJob: Boolean)

data class Step @JsonCreator constructor(
        val name: String,
        val actionOnFailure: ActionOnFailure,
        val jarPath: String)

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
