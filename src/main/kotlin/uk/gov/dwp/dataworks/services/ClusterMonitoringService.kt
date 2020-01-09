package uk.gov.dwp.dataworks.services

import org.springframework.stereotype.Service
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.emr.EmrAsyncClient
import software.amazon.awssdk.services.emr.model.DescribeClusterRequest

@Service
class ClusterMonitoringService {
    fun getClusterStatus(clusterId: String) : String {
        val emrClient = EmrAsyncClient.builder().region(Region.EU_WEST_1).build()
        val response = emrClient.describeCluster(DescribeClusterRequest.builder().clusterId(clusterId).build())
        return response.get().cluster().status().stateAsString()
    }
}
