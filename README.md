# Sensor Microagent

This is the Java implementation of the Sensor Microagent of [Immunizer: The Collaborative Cloud-based Unsupervised Software Immunity Framework](https://github.com/oiraqi/immunizer)

## Siblings
### Autonomic Protection Microagents
- [Sensor Microagent](https://github.com/oiraqi/immunizer-sensor)
- [Effector Microagent](https://github.com/oiraqi/immunizer-effector)
### Autonomic Protection Microservices
- [Monitoring Microservice](https://github.com/oiraqi/immunizer-monitor)
- [Analysis Microservice](https://github.com/oiraqi/immunizer-analyze)
- [Planning Microservice](https://github.com/oiraqi/immunizer-plan)
- [Execution Microservice](https://github.com/oiraqi/immunizer-execute)
- [Collaboration Microservice](https://github.com/oiraqi/immunizer-collaborate)
- [Dashboard Microservice](https://github.com/oiraqi/immunizer-dashboard)

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
- docker build -t immunizer-microagents:1.0 https://raw.githubusercontent.com/oiraqi/immunizer-monitoring/master/docker/Dockerfile
- docker run --name immunizer-microagents-container -p 8443:8443 -it immunizer-microagents:1.0
- cd immunizer-monitoring/scripts
- ./ofbiz-immunized.sh
