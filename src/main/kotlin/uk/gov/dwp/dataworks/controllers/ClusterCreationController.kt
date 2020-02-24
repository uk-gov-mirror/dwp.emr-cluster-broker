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
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.dwp.dataworks.model.CreationRequest
import uk.gov.dwp.dataworks.model.InvalidJobFlowRoleException
import uk.gov.dwp.dataworks.services.ClusterCreationService
import uk.gov.dwp.dataworks.services.PrometheusMetricsService
import java.util.UUID

@RestController
class ClusterCreationController {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClusterCreationController::class.java)
    }

    @Autowired
    private lateinit var clusterCreationService: ClusterCreationService

    @Autowired
    private lateinit var prometheusMetricsService: PrometheusMetricsService

    @Operation(summary = "Submit EMR request", description = "Submits a request to AWS for the creation of an EMR " +
            "cluster and executes the provided steps in the new cluster.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "String", examples = [ExampleObject("j-1K48XXXXXXHCB")])]),
        ApiResponse(responseCode = "400", description = "Failure")
    ])
    @PostMapping("/cluster/submit")
    @ResponseStatus(HttpStatus.OK)
    fun submitRequestToCluster(@RequestBody requestBody: CreationRequest): String {
        logger.info("Received submit event with name: ${requestBody.name} and steps ${requestBody.steps.map {it.name}}.")
        val clusterId = "${requestBody.name}-${UUID.randomUUID()}"

        if(clusterCreationService.jobFlowRoleIsBlacklisted(requestBody.jobFlowRole)) {
            throw InvalidJobFlowRoleException("JobFlowRole ${requestBody.jobFlowRole} is blacklisted and shouldn't be used")
        }
        clusterCreationService.submitStepRequest(clusterId, requestBody)
        prometheusMetricsService.incrementCounter("clusters_created")

        logger.info("Submitted request $clusterId.")
        return clusterId
    }

    @ExceptionHandler(InvalidJobFlowRoleException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "JobFlowRole is blacklisted and shouldn't be used")
    fun handleInvalidJobFlowRole() {
        // Do nothing - annotations handle response
    }
}
