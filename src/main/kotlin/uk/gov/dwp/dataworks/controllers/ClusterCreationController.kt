package uk.gov.dwp.dataworks.controllers

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
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
import uk.gov.dwp.dataworks.services.ClusterCreationService
import java.util.UUID

@RestController
class ClusterCreationController {
    @Autowired
    private lateinit var clusterCreationService: ClusterCreationService

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    fun submitRequestToCluster(@RequestBody requestBody: CreationRequest): String {
        logger.info("Received submit event with name: ${requestBody.name} and steps ${requestBody.steps.map {it.name}}")

        val clusterId = "${requestBody.name}-${UUID.randomUUID()}"
        clusterCreationService.submitStepRequest(clusterId, requestBody)

        logger.info("Submitted request $clusterId.")
        return clusterId
    }

    @ExceptionHandler(InvalidDefinitionException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Malformed request - cannot unmarshall POST request body")
    fun handleMalformedRequest() {
        // Do nothing - annotations handle response
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ClusterCreationController::class.java)
    }
}
