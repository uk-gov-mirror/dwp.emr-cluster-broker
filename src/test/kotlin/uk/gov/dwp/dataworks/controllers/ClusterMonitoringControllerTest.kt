package uk.gov.dwp.dataworks.controllers

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.dwp.dataworks.services.ClusterMonitoringService
import uk.gov.dwp.dataworks.services.PrometheusMetricsService

@RunWith(SpringRunner::class)
@WebMvcTest(ClusterMonitoringController::class)
class ClusterMonitoringControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc
    @MockBean
    private lateinit var prometheusMetricsService: PrometheusMetricsService
    @MockBean
    private lateinit var clusterMonitoringService: ClusterMonitoringService

    private val expectedStatusReturnValue = "RUNNING"

    @Test
    fun `Status endpoint returns '405 not supported' for POST requests`() {
        mvc.perform(post("/cluster/status/abc"))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `Status endpoint returns '404 Not Found' when request has no {id}`() {
        mvc.perform(get("/cluster/status"))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `Returns status request successfully with proper status request`() {
        whenever(clusterMonitoringService.getClusterStatus(anyString())).doReturn(expectedStatusReturnValue)

        mvc.perform(get("/cluster/status/test-cluster-id"))
                .andExpect(status().isOk)
                .andExpect(content().string(expectedStatusReturnValue))
    }
}
