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
