# EMR Cluster Broker
A lightweight Spring Boot API to pass-through requests to AWS SDK and create EMR clusters. This API is going to be part of the backbone of the architecture in analytical and batch jobs. The API will allow on-demand, opinionated clusters to be launched when required for analytics or regular jobs. Sizes are templated and are found in `/src/main/resources/instanceConfigurations`.

Future case studies will drive more requirements.

## Building
emr-cluster-broker takes advantage of Docker to run the API in a lightweight and portable manner. 

### How to build
To build the Docker image to a local Docker daemon, run the following from the root of the project.
```
./gradlew clean build
docker build --tag dwpdigital/emr-cluster-broker:latest .
```
And to start the image:
```
docker run -p 8080:8080  -e AWS_REGION=<VALUE> -e AMI_SEARCH_PATTERN=<VALUE> -e AMI_OWNER_IDS=<VALUE> -e EMR_RELEASE_LABEL=<VALUE> -e S3_LOG_URI=<VALUE> -e SECURITY_CONFIGURATION=<VALUE> -e JOB_FLOW_ROLE_BLACKLIST=<VALUE>   AWS_REGION="testRegion" dwpdigital/emr-cluster-broker
```
Ensuring that `<VALUE>` is replaced with a suitable value for that `ConfigKey` entry.

## Configuration
Although the Cluster Broker uses Spring we want to provide a way of deploying and configuring the application via tools such as Terraform. As a result, we substitute configuration items with a `ConfigurationService` class that will resolve config via env vars.

Possible configurations known to the app can be found in the `ConfigKey` enum. If any of these are not present at runtime the app will not fail until the item has been resolved by the service.

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
 