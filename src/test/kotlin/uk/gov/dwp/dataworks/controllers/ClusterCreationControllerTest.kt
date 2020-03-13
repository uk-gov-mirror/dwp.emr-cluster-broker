package uk.gov.dwp.dataworks.controllers

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.contains
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import software.amazon.awssdk.services.emr.model.ActionOnFailure
import software.amazon.awssdk.services.emr.model.Configuration
import uk.gov.dwp.dataworks.model.*
import uk.gov.dwp.dataworks.services.ClusterCreationService
import uk.gov.dwp.dataworks.services.PrometheusMetricsService


@RunWith(SpringRunner::class)
@WebMvcTest(ClusterCreationController::class)
class ClusterCreationControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc
    @MockBean
    private lateinit var prometheusMetricsService: PrometheusMetricsService
    @MockBean
    private lateinit var clusterCreationService: ClusterCreationService

    @BeforeEach
    fun setup() {
        doNothing().whenever(clusterCreationService.submitStepRequest(any(), any()))
        doNothing().whenever(clusterCreationService.jobFlowRoleIsBlacklisted(any()))
    }

    @Test
    fun `Submit endpoint returns '405 not supported' for GET requests`() {
        mvc.perform(get("/cluster/submit"))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `Submit endpoint returns status malformed when request fails contract`() {
        mvc.perform(post("/cluster/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"abc\"}"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `Submits request successfully with proper POST request`() {
        mvc.perform(post("/cluster/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testCreationRequest()))
                .andExpect(status().isOk)
        verify(clusterCreationService, times(1)).submitStepRequest(contains("testCluster"), any())
    }

    @Test
    fun `Submit endpoint returns invalid jobFlowRole when role is blacklisted`() {
        whenever(clusterCreationService.jobFlowRoleIsBlacklisted(any())).thenReturn(true)
        mvc.perform(post("/cluster/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testCreationRequest()))
                .andExpect(status().isBadRequest)
                .andExpect(status().reason("JobFlowRole is blacklisted and shouldn't be used"))
    }

    fun testCreationRequest(): String {
        val testRequest = CreationRequest(
                "testCluster",
                "releaseLabel",
                "testServiceRole",
                "testFlowRole",
                "testAutoScalingRole",
                "testHostedZoneId",
                CustomInstanceConfig("0.0.0.0/0", false, InstanceTemplate.SMALL,false),
                listOf(EmrConfiguration("testClassification", mapOf("testProperty" to "testValue"))),
                listOf(
                        Step("testStep1", ActionOnFailure.TERMINATE_JOB_FLOW, "test/jar/path/1", listOf("arg1", "arg2")),
                        Step("testStep2", ActionOnFailure.CONTINUE, "test/jar/path/2", listOf("arg1", "arg2"))),
                listOf("spark", "hdfs", "test"))
        return ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY).writeValueAsString(testRequest)
    }
}
