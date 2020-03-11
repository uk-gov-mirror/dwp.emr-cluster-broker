# EMR Cluster Broker
A lightweight Spring Boot API to pass-through requests to AWS SDK and create EMR clusters. This API is going to be part of the backbone of the architecture in analytical and batch jobs. The API will allow on-demand, opinionated clusters to be launched when required for analytics or regular jobs. Sizes are templated and are found in `/src/main/resources/instanceConfigurations`.

Future case studies will drive more requirements.

## Building
emr-cluster-broker takes advantage of Docker to run the API in a lightweight and portable manner. 	

### How to build
To build the Docker image to a local Docker daemon, run the following from the root of the project.
```
./gradlew jibDockerBuild
```
This task can also be added as a dependency on the build by adding the following to your `build.gradle.kts` file.
```
./gradlew clean build
docker build --tag dwpdigital/emr-cluster-broker:latest .
```

To start the image:
```
docker run -p 8443:8443 -e clusterBroker_awsRegion=<VALUE> -e clusterBroker_amiSearchPattern=<VALUE> -e clusterBroker_amiOwnerIds=<VALUE> -e clusterBroker_emrReleaseLabel=<VALUE> -e clusterBroker_s3LogUri=<VALUE> -e clusterBroker_securityConfiguration=<VALUE> -e clusterBroker_jobFlowRoleBlacklist=<VALUE> -e clusterBroker_jobFlowRole=<VALUE> -e clusterBroker_serviceRole=<VALUE> -e clusterBroker_autoScalingRole=<VALUE> -e clusterBroker_hostedZoneID=<VALUE> dwpdigital/emr-cluster-broker:latest
```
Ensuring that `<VALUE>` is replaced with a suitable value for that `ConfigKey` entry.

## Sending API requests
Since [#17](https://github.com/dwp/emr-cluster-broker/pull/17) the broker only accepts connections over HTTPS.

### Submitting a Cluster creation step
To submit a request to the cluster broker for a new cluster the `/cluster/submit`endpoint needs to be `POST`-ed to. The content of the `POST` request is defined by the `CreationRequest` class. An example request is as follows:
```json
{
  "name": "test-cluster",
  "releaseLabel": "emr-5.28.0",
  "serviceRole": "arn:aws:iam::00000000000:role/service_role",
  "jobFlowRole": "arn:aws:iam::00000000000:instance-profile/AE_EMR_EC2_Role",
  "autoScalingRole": "arn:aws:iam::00000000000:role/auto_scaling_role",
  "customInstanceConfig": {
    "ec2SubnetId": "subnet-0000aaaa00a0000a0",
    "useSpotPricing": false,
    "instanceTemplate": "SMALL",
    "keepAlivePostJob": true
  },
  "customEmrConfigs": [
    {
    "classification": "custom-classication",
    "properties": {
      "custom-property": "custom-value"
      }
    }
  ],
  "steps": [
    {
      "name": "emr-setup",
      "actionOnFailure": "CONTINUE",
      "jarPath": "s3://s3-bucket-id/jar/prefix"
    }
  ],
  "applications": [
    "Spark"
  ]
}
``` 

### Parameter Reference
`serviceRole`, `jobFlowRole` and `autoScalingRole` are optional - exclusion will revert to default values set by the cluster broker.

## API Configuration
Although the Cluster Broker uses Spring we want to provide a way of deploying and configuring the application via tools such as Terraform. As a result, we substitute configuration items with a `ConfigurationService` class that will resolve config via env vars.

Possible configurations known to the app can be found in the `ConfigKey` enum. If any of these are not present at runtime the app will not fail until the item has been resolved by the service.

## API Documentation
Our API follows the [Open API](https://github.com/OAI/OpenAPI-Specification) specification to gain the benefit of the API self-describing itself, from endpoints and request methods to example responses.

### Implementation
[Swagger Annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations) in `@Controller` classes are used to provide the OpenAPI specification for our endpoints.

[ReDoc](https://github.com/Redocly/redoc) is used to display the OpenAPI specification. This is provided as a static webpage at the root of the server. To see the documentation:

Locally:
```
Run Application.kt -> Navigate to https://localhost:8443/
OR
Build Docker container -> Run container & expose port 8443 -> Navigate to https://localhost:<exposed-port>/ 
```

In Deployed instance
```
Navigate to root of deployed instance.
```

## Monitoring
The API has two-fold monitoring available to it - both provided via Spring.

Default metrication is handled by [Spring Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html). [All endpoints](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-endpoints) not remapped (See below) can be found on their respective paths under the root `/`. Some endpoints have been remapped to conform to standards and avoid clashing, refer to `/resrouces/applciation.properties` for details on remapped endpoints 

Any custom metrication is provided with the help of [Spring metrics](https://docs.spring.io/spring-metrics/docs/current/public/prometheus), whereby a [Prometheus](https://prometheus.io/) compliant endpoint is provided. To expose custom metrics to the prometheus pull api, the `PrometheusMetricsService` class should be used across the project. 
 
## Deployment
Cluster Broker uses [Terraform](https://www.terraform.io/) to handle deployments to [AWS Fargate](https://aws.amazon.com/fargate). Terraform code can be found in `terraform/deploy`.

*Note that no `.tfvars` files should be committed to the repository due to the potential revelation of secrets. See [secrets](#Secrets) for further information*

### Deployment Steps

1. Use `make` to download variables & interpret Jinja templates ([See below](#Secrets))
    ```
    make bootstrap
    ```

#### Please note, the following steps need to be carried out in both terraform/deploy/infra/, and terraform/deploy/apps/cluster_broker/

2. Initialise Terraform
    ```
    terraform init
    ```
3. Run Terraform plan and ensure everything is as expected
    ```
    terraform plan
    ```
4. Once the plan is assured, apply the changes
    ```
    terraform apply
    ```
 
### Secrets
For Terraform to deploy the service, we ned to include information which we do not want to commit. To enable us to use these values in Terraform code we have a variable inside AWS parameter store. Python and [Jinja2](https://jinja.palletsprojects.com/en/2.10.x/) are used to download and insert the values into a template and write out the final TF code.

[Make](https://www.gnu.org/software/make/) is used to download & interpolate variables and create the required `.tf` files:
```
$ make bootstrap
```
The Make file has the following dependencies:
- Python3 & pip3.
- Terraform
- AWS credentials allowing download of the variables from AWS.

### Cluster Broker Naming Conventions

_Note, not all of this functionality has been implemented in the cluster broker yet, however as it is implemented should follow the standards below_

- EMR Cluster
  - cluster-name-<uuid>
- IAM Roles
  - cb-<role>-<uuid>
- Security Groups
  - cb-<uuid>
- DNS Records
  - <aws-cluster-id>.<domain>
- Log (bucket path not the bucket itself)
  - s3://path/logs/<aws-cluster-id>
