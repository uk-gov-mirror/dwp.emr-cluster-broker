package uk.gov.dwp.dataworks.services

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.emr.EmrAsyncClient
import software.amazon.awssdk.services.emr.EmrClient
import software.amazon.awssdk.services.emr.model.ClusterSummary
import software.amazon.awssdk.services.emr.model.DescribeClusterRequest
import java.util.stream.Collectors

@Service
class ClusterMonitoringService {
    @Autowired
    private lateinit var configService: ConfigurationService

    fun getClusterStatus(clusterId: String): String {
        val emrClient = EmrAsyncClient.builder().region(configService.awsRegion).build()
        val response = emrClient.describeCluster(DescribeClusterRequest.builder().clusterId(clusterId).build())
        return response.get().cluster().status().stateAsString()
    }

    fun listAllClusters(): String {
        val objectMapper = ObjectMapper(JsonFactory())

        val emrClient = EmrClient.builder().region(configService.awsRegion).build()
        val rawResponses = emrClient.listClustersPaginator().clusters().stream().collect(Collectors.toList())
        val responses = rawResponses.filter { it.name().startsWith("cb-") }
                .sortedWith(compareBy<ClusterSummary> { it.status().stateAsString() }.thenBy { it.name() })
                .map { it.toBuilder() }
        return objectMapper.writeValueAsString(responses)
    }
}
