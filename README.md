# Instrumentation Microagent

This is the Java implementation of the Instrumentation Microagent of [Immunizer: The Collaborative Cloud-based Unsupervised Software Immunity Framework](https://github.com/oiraqi/immunizer)

## Siblings
- [Acquisition Microservice](https://github.com/oiraqi/immunizer-acquisition)
- [Analysis Microservice](https://github.com/oiraqi/immunizer-analysis)
- [Collaboration Microservice](https://github.com/oiraqi/immunizer-collaboration)

## Dependencies

All dependencies are managed through Docker and Gradle. Docker is all what you need, while Gradle distribution will be automatically fetched by Gradle wrapper, which will be downloaded from this repository by Docker.

## Structure
- docker: hosts Dockerfile, all what you need to build your development and test environment
- framework: source code and dependencies managed by Gradle
- scripts: folder containing build/run scripts

## Current Environment / Docker Image
- Linux Ubuntu 18.04 (Bionic)
- OpenJDK 8
- ByteBuddy 1.10.3
- Gson 2.8.6
- Apache Kafka Clients API 2.4.0
- OFBiz (nightly built)

## How To
- Make sure you have Docker installed
- docker build -t immune-apps:1.0 https://raw.githubusercontent.com/oiraqi/immunizer-instrumentation/master/docker/Dockerfile
- docker run --name immune-apps-container -p 8443:8443 -it immune-apps:1.0
- cd immune-apps/scripts
- ./ofbiz-immunized.sh
