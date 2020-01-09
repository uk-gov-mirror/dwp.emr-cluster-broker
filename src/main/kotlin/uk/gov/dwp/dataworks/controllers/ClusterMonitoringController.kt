package uk.gov.dwp.dataworks.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.dwp.dataworks.services.ClusterMonitoringService
import uk.gov.dwp.dataworks.services.PrometheusMetricsService

@RestController
class ClusterMonitoringController {
    @Autowired
    private lateinit var prometheusMetricsService: PrometheusMetricsService

    @Autowired
    private lateinit var clusterMonitoringService: ClusterMonitoringService

    @Operation(summary = "Get cluster status by ID", description = "Requests the status of the provided clusterId by" +
            "sending a request to the AWS SDK. Including no clusterId will result in a failure.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "String", examples = [ExampleObject("RUNNING")])]),
        ApiResponse(responseCode = "400", description = "Failure")
    ])
    @GetMapping("/cluster/status/{clusterId}")
    @ResponseStatus(HttpStatus.OK)
    fun listClusterStatus(@PathVariable clusterId: String): String {
        logger.debug("Received status event for cluster: $clusterId")
        prometheusMetricsService.incrementCounter("status_calls")
        return clusterMonitoringService.getClusterStatus(clusterId)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClusterMonitoringController::class.java)
    }
}
