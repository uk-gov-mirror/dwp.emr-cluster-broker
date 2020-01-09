package uk.gov.dwp.dataworks.services

import io.micrometer.core.instrument.Counter
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PrometheusMetricsService {

    @Autowired
    private lateinit var meterRegistry: PrometheusMeterRegistry

    fun incrementCounter(counterName: String, incrementAmount: Double = 1.0) {
        var counter = meterRegistry.find(counterName).counter()
        if(counter == null) {
            counter = Counter.builder(counterName)
                    .tag("source", "cluster_broker")
                    .register(meterRegistry)
        }
        counter.increment(incrementAmount)
    }
}
