[![Build Status](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.iudx.io%3A8080%2Fjob%2Fiudx%2520data-ingestion-server%2520%28master%29%2520pipeline%2F)](http://jenkins.iudx.io:8080/job/iudx%20data-ingestion-server%20(master)%20pipeline//lastBuild/)
[![Jenkins Coverage](https://img.shields.io/jenkins/coverage/jacoco?jobUrl=http%3A%2F%2Fjenkins.iudx.io%3A8080%2Fjob%2Fiudx%2520data-ingestion-server%2520%28master%29%2520pipeline%2F)](http://jenkins.iudx.io:8080/job/iudx%20data-ingestion-server%20(master)%20pipeline//lastBuild/jacoco/)
[![Unit Tests](https://img.shields.io/jenkins/tests?jobUrl=http%3A%2F%2Fjenkins.iudx.io%3A8080%2Fjob%2Fiudx%2520data-ingestion-server%2520%28master%29%2520pipeline%2F)](http://jenkins.iudx.io:8080/job/iudx%20data-ingestion-server%20(master)%20pipeline//lastBuild/testReport/)
[![Performance Tests](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.iudx.io%3A8080%2Fjob%2Fiudx%2520data-ingestion-server%2520%28master%29%2520pipeline%2F)](http://jenkins.iudx.io:8080/job/iudx%20data-ingestion-server%20(master)%20pipeline//lastBuild/performance/)
[![Security Tests](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.iudx.io%3A8080%2Fjob%2Fiudx%2520data-ingestion-server%2520%28master%29%2520pipeline%2F)](http://jenkins.iudx.io:8080/job/iudx%20data-ingestion-server%20(master)%20pipeline//lastBuild/zap/)
[![Integration Tests](https://img.shields.io/jenkins/build?jobUrl=http%3A%2F%2Fjenkins.iudx.io%3A8080%2Fjob%2Fiudx%2520data-ingestion-server%2520%28master%29%2520pipeline%2F)](http://jenkins.iudx.io:8080/job/iudx%20data-ingestion-server%20(master)%20pipeline//HTML_20Report/)

![IUDX](./docs/iudx.png)
# iudx-data-ingestion-server
The <b>Data Ingestion Server</b> is the "Ingestion Firewall and Data Cleaning Middleware" of [IUDX](https://iudx.org.in). It enables *Providers* and *Delegates* to publish data using the IUDX API as per the data descriptor using the <b>HTTP protocol over TLS</b>(HTTPs).

<p align="center">
<img src="docs/di_server_overview.jpg">
</p>

## **Features**

-  Data Ingestion Server allows IUDX Data *Providers* and *Delegate* to publish data into the IUDX platform
- Allows IUDX admin to register and delete ingestion stream for one or more data resources using standard APIs
- Integrated with IUDX authorization server (token introspection) to allow data publication
- Secure data publication over TLS.
- Scalable, service mesh architecture based implementation using open source components: Vert.X API framework and RabbitMQ for data broker.
- Hazelcast and Zookeeper based cluster management and service discovery.


## API Docs 
The api docs can be found [here] *need to add the link here*.

## Get Started

### Prerequisite - Make configuration
Make a config file based on the template in `./configs/config-example.json` 
- Generate a certificate using Lets Encrypt or other methods
- Make a Java Keystore File and mention its path and password in the appropriate sections
- Modify the database url and associated credentials in the appropriate sections

### Docker based
1. Install docker and docker-compose
2. Clone this repo
3. Build the images 
   ` ./docker/build.sh`
4. Modify the `docker-compose.yml` file to map the config file you just created
5. Start the server in production (prod) or development (dev) mode using docker-compose 
   ` docker-compose up prod `


### Maven based
1. Install java 11 and maven
2. Use the maven exec plugin based starter to start the server 
   `mvn clean compile exec:java@data-ingestion-server`

### Testing

### Unit tests
1. Run the server through either docker, maven or redeployer
2. Run the unit tests and generate a surefire report 
   `mvn clean test-compile surefire:test surefire-report:report`
3. Reports are stored in `./target/`

### Integration tests
Integration tests are through Postman/Newman whose script can be found from [here](src/test/resources/Data_Ingestion.postman_collection.json).
1. Install prerequisites
   - [postman](https://www.postman.com/) + [newman](https://www.npmjs.com/package/newman)
   - [newman reporter-htmlextra](https://www.npmjs.com/package/newman-reporter-htmlextra)
2. Example Postman environment can be found [here](src/test/resources/ingest.iudx.io.postman_environment.json)
3. Run the server through either docker, maven or redeployer
4. Run the integration tests and generate the newman report 
   `newman run <postman-collection-path> -e <postman-environment> --insecure -r htmlextra --reporter-htmlextra-export .`
5. Reports are stored in `./target/`

## Contributing
We follow Git Merge based workflow 
1. Fork this repo.
2. Create a new feature branch in your fork. Multiple features must have a hyphen separated name, or refer to a milestone name as mentioned in Github -> Projects.
3. Commit to your fork and raise a Pull Request with upstream.

## License
[MIT](LICENSE)
