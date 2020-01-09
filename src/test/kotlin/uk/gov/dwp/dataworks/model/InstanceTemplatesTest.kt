package uk.gov.dwp.dataworks.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import software.amazon.awssdk.services.emr.model.JobFlowInstancesConfig

class InstanceTemplatesTest {
    @Test
    fun `Ensure all instance templates are present`() {
        InstanceTemplate.values()
                .map { it.fileName }
                .forEach {
                    val expectedFile = this::class.java.getResource("/instanceConfigurations/${it}")
                    assertThat(expectedFile).withFailMessage("Did not find expected file $it").isNotNull()
                }
    }

    @Test
    fun `Ensure all instance templates are well formed`() {
        assertThatCode {
            InstanceTemplate.values()
                    .map { it.fileName }
                    .map { this::class.java.getResourceAsStream("/instanceConfigurations/$it") }
                    .map { ObjectMapper().readValue(it, JobFlowInstancesConfig.serializableBuilderClass()) }
        }.doesNotThrowAnyException()
    }

    @Test
    fun `Enum class can deserialise each template file`() {
        assertThatCode {
            InstanceTemplate.values()
                    .forEach { it.get() }
        }.doesNotThrowAnyException()
    }
}
