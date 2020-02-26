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
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.dwp.dataworks.services.ClusterMonitoringService
import uk.gov.dwp.dataworks.services.PrometheusMetricsService

@RestController
class ClusterMonitoringController {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClusterMonitoringController::class.java)
    }

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

    @Operation(summary = "Get all clusters created by the broker", description = "Retrieves the full list of clusters" +
            "created by the cluster broker, based on the tag which is appended to instances.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "application/json", examples = [ExampleObject("""[{"id": "j-A000AAAA00AA","name": "cb-created-cluster-<UUID>","status": {"stateChangeReason": {"message": "Terminated by user request","codeAsString": "USER_REQUEST"},"timeline": {"creationDateTime": {"nano": 000000000,"epochSecond": 0000000000},"readyDateTime": {"nano": 000000000,"epochSecond": 0000000000},"endDateTime": {"nano": 000000000,"epochSecond": 0000000000}},"stateAsString": "TERMINATED"},"normalizedInstanceHours": 64,"clusterArn": "arn:aws:elasticmapreduce:us-east-1:000000000000:cluster/j-A000AAAA00AA","outpostArn": null}]""")])]),
        ApiResponse(responseCode = "400", description = "Failure")
    ])
    @GetMapping("/cluster/list", produces = [ MediaType.APPLICATION_JSON_VALUE ])
    @ResponseStatus(HttpStatus.OK)
    fun listAllClusters(): String {
        logger.debug("Received list cluster status event.")
        return clusterMonitoringService.listAllClusters()
    }
}
