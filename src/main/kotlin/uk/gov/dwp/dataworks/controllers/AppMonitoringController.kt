package uk.gov.dwp.dataworks.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.dwp.dataworks.services.ConfigurationService

@RestController
class AppMonitoringController {

    @Autowired
    private lateinit var configurationService: ConfigurationService

    @Operation(summary = "List all env vars", description = "Lists all of the env vars currently configured in JSON format")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Success", content = [Content(mediaType = "String", examples = [ExampleObject("{\"AWS_REGION\":\"testRegion\"}")])]),
        ApiResponse(responseCode = "400", description = "Failure")
    ])
    @GetMapping("/broker/environment")
    @ResponseStatus(HttpStatus.OK)
    fun listEnvironmentVars(): String {
        return configurationService.getAllConfig().map { "\"${it.key}\":\"${it.value}\"" }.joinToString(separator = ",", prefix = "{", postfix = "}")
    }
}
