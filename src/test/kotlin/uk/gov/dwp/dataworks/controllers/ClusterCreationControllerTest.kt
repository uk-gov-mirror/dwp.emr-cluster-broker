package uk.gov.dwp.dataworks.controllers

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
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
import uk.gov.dwp.dataworks.model.CreationRequest
import uk.gov.dwp.dataworks.model.CustomInstanceConfig
import uk.gov.dwp.dataworks.model.InstanceTemplate
import uk.gov.dwp.dataworks.model.Step
import uk.gov.dwp.dataworks.services.ClusterCreationService

@RunWith(SpringRunner::class)
@WebMvcTest(ClusterCreationController::class)
class ClusterCreationControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var clusterCreationService: ClusterCreationService

    @BeforeEach
    fun setup() {
        doNothing().`when`(clusterCreationService.submitStepRequest(any(), any()))
    }

    @Test
    fun `Submit endpoint returns '405 not supported' for GET requests`() {
        mvc.perform(get("/submit"))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `Submit endpoint returns status malformed when request fails contract`() {
        mvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"abc\"}"))
                .andExpect(status().isBadRequest)
                .andExpect(status().reason("Malformed request - cannot unmarshall POST request body"))
    }

    @Test
    fun `Submits request successfully with proper POST request`() {
        mvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(stubCreationRequest()))
                .andExpect(status().isOk)
        verify(clusterCreationService, times(1)).submitStepRequest(contains("testCluster"), any())
    }

    fun stubCreationRequest(): String {
        val testRequest = CreationRequest(
                "testCluster",
                "releaseLabel",
                "test::uri",
                "testSC",
                "testServiceRole",
                "testFlowRole",
                CustomInstanceConfig(false, InstanceTemplate.SMALL,false),
                listOf(
                        Step("testStep1", ActionOnFailure.TERMINATE_JOB_FLOW, "test/jar/path/1"),
                        Step("testStep2", ActionOnFailure.CONTINUE, "test/jar/path/2")),
                listOf("spark", "hdfs", "test"))
        return ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY).writeValueAsString(testRequest)
    }
}
