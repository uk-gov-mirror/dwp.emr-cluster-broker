# EMR Cluster Broker
A lightweight Spring Boot API to passthrough requests to AWS SDK and create EMR clusters. This API is going to be part of the backbone of the architecture in analytical and batch jobs. The API will allow on-demand, opinionated clusters to be launched when required for analytics or regular jobs. Sizes are templated and are found in `/src/main/resources/instanceConfigurations`.

Future case studies will drive more requirements.

## Building
emr-cluster-broker takes advantage of Docker to run the API in a lightweight and portable manner. You will note that there is no `Dockerfile` present in this repo. The reason for this is that we are using [Google's Jib](https://github.com/GoogleContainerTools/jib)

### Why Jib?
Speed, size, security... jib builds container images much, much faster than a docker build process. They’re much smaller, as the images are practically just a JVM (there’s essentially no OS - they are `distroles`). As there’s essentially no OS, they have a much smaller potential vulnerability footprint, which is great from a security perspective.

### How to build
Docker images can be built directly from Gradle. To create a local docker image, run:
```
gradle jibDockerBuild
``` 
This task can also be added as a dependency on the build by adding the following to your `build.gradle.kts` file.
```
tasks.named("build") {
    dependsOn(":jibDockerBuild")
}
```

## API Documentation
Our API follows the [Open API](https://github.com/OAI/OpenAPI-Specification) specification to gain the benefit of the API self-describing itself, from endpoints and request methods to example responses.

### Implementation
[Swagger Annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations) in `@Controller` classes are used to provide the OpenAPI specification for our endpoints.

[ReDoc](https://github.com/Redocly/redoc) is used to display the OpenAPI specification. This is provided as a static webpage at the root of the server. To see the documentation:

Locally:
```
Run Application.kt -> Navigate to localhost:8080/
OR
Build Docker container -> Run container & expose port 8080 -> Navigate to localhost:<exposed-port>/ 
```

In Deployed instance
```
Navigate to root of deployed instance.
```

## Monitoring
The API has two-fold monitoring available to it - both provided via Spring.

Default metrication is handled by [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html), providing standard metrication of spring to numerous endpoints under `/actuator`.

Any custom metrication is provided with the help of [Spring metrics](https://docs.spring.io/spring-metrics/docs/current/public/prometheus), whereby a [Prometheus](https://prometheus.io/) compliant endpoint is provided. To expose custom metrics to the prometheus pull api, the `PrometheusMetricsService` class should be used across the project. 
 