[![Build Status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520data-ingestion-server%2520%28v4.0.0%29%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20data-ingestion-server%20(v4.0.0)%20pipeline//lastBuild/)
[![Jenkins Coverage](https://img.shields.io/jenkins/coverage/jacoco?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520data-ingestion-server%2520%28v4.0.0%29%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20data-ingestion-server%20(v4.0.0)%20pipeline//lastBuild/jacoco/)
[![Unit Tests](https://img.shields.io/jenkins/tests?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520data-ingestion-server%2520%28v4.0.0%29%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20data-ingestion-server%20(v4.0.0)%20pipeline//lastBuild/testReport/)
[![Performance Tests](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520data-ingestion-server%2520%28v4.0.0%29%2520pipeline%2F&label=performance%20tests)](https://jenkins.iudx.io/job/iudx%20data-ingestion-server%20(v4.0.0)%20pipeline//lastBuild/performance/)
[![Security Tests](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520data-ingestion-server%2520%28v4.0.0%29%2520pipeline%2F&label=security%20tests)](https://jenkins.iudx.io/job/iudx%20data-ingestion-server%20(v4.0.0)%20pipeline//lastBuild/zap/)
[![Integration Tests](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520data-ingestion-server%2520%28v4.0.0%29%2520pipeline%2F&label=integration%20tests)](https://jenkins.iudx.io/job/iudx%20data-ingestion-server%20(v4.0.0)%20pipeline/Integration_20Test_20Report/)

![IUDX](./docs/iudx.png)
# iudx-data-ingestion-server
The <b>Data Ingestion Server</b> is the "Ingestion Firewall and Data Cleaning Middleware" of [IUDX](https://iudx.org.in). It enables *Providers* and *Delegates* to publish data using the IUDX API as per the data descriptor using the <b>HTTP protocol over TLS</b>(HTTPs).

<p align="center">
<img src="docs/di_server_overview.png">
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

## Prerequisites

### External dependencies installation

The Data ingestion pipeline connects to various external dependencies namely
 - RabbitMQ
 - ImmuDB

Find the installations of the above along with the configurations to modify the database url, port and associated credentials in the appropriate sections [here](SETUP.md)

## Get Started

### Make configuration
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

### JAR based
1. Install java 11 and maven
2. Set Environment variables
```
export DI_URL=https://<rs-domain-name>
export LOG_LEVEL=INFO
```
3. Use maven to package the application as a JAR
   `mvn clean package -Dmaven.test.skip=true`
4. 2 JAR files would be generated in the `target/` directory
   - `iudx.data.ingestion.server-cluster-0.0.1-SNAPSHOT-fat.jar` - clustered vert.x containing micrometer metrics
   - `iudx.data.ingestion.server-dev-0.0.1-SNAPSHOT-fat.jar` - non-clustered vert.x and does not contain micrometer metrics
#### Running the clustered JAR
**Note**: The clustered JAR requires Zookeeper to be installed. Refer [here](https://zookeeper.apache.org/doc/r3.3.3/zookeeperStarted.html) to learn more about how to set up Zookeeper. Additionally, the `zookeepers` key in the config being used needs to be updated with the IP address/domain of the system running Zookeeper.
The JAR requires 3 runtime arguments when running:
* --config/-c : path to the config file
* --hostname/-i : the hostname for clustering
* --modules/-m : comma separated list of module names to deploy
  e.g. `java -jar ./fatjar.jar --host $(hostname) -c configs/config.json -m
iudx.data.ingestion.server.ApiServerVerticle`
  Use the `--help/-h` argument for more information. You may additionally append an `DI_JAVA_OPTS` environment variable containing any Java options to pass to the application.
  e.g.
```
$ export RS_JAVA_OPTS="-Xmx4096m"
$ java $RS_JAVA_OPTS -jar target/iudx.data.ingestion.server-cluster-0.0.1-SNAPSHOT-fat.jar ...
```
#### Running the non-clustered JAR
The JAR requires 1 runtime argument when running:
* --config/-c : path to the config file
  e.g. `java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4j2LogDelegateFactory -jar target/iudx.data.ingestion.server-dev-0.0.1-SNAPSHOT-fat.jar -c configs/config.json`
  Use the `--help/-h` argument for more information. You may additionally append an `RS_JAVA_OPTS` environment variable containing any Java options to pass to the application.
  e.g.
```
$ export RS_JAVA_OPTS="-Xmx1024m"
$ java $RS_JAVA_OPTS -jar target/iudx.data.ingestion.server-dev-0.0.1-SNAPSHOT-fat.jar ...
```


### Testing

### Unit tests
1. Run the server through either docker, maven or redeployer
2. Run the unit tests and generate a surefire report 
   `mvn clean test-compile surefire:test surefire-report:report`
3. Reports are stored in `./target/`

### Integration tests
Integration tests are through Postman/Newman whose script can be found from [here](src/test/resources/IUDX_Data_Ingestion_Server_V3.5.postman_collection.json).
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
