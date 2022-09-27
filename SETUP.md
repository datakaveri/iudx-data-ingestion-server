Setup Guide
----

This document contains the installation and configuration processes of the external modules of each Verticle in IUDX Data Ingestion Pipeline Server.

<p align="center">
<img src="docs/di_server_overview.png">
</p>


Data Ingestion Pipeline server connects with various external dependencies namely :

 - `RabbitMQ` : used to publish and subscribe different types of messages or events.
 - `ImmuDB` : used to store metering related information.
 
 
## Setting up RabbitMQ
 
> Refer to the docker files available [here](https://github.com/datakaveri/iudx-deployment/blob/master/Docker-Swarm-deployment/single-node/databroker) to 
> setup RMQ.


In order to connect to the appropriate RabbitMQ instance, required information such as dataBrokerIP,dataBrokerPort etc. should be updated in the DataBrokerVerticle module available in [config-example.json](example-configs/config-example.json).


**DataBroker Verticle**

```
{
    "id": "iudx.data.ingestion.server.databroker.DataBrokerVerticle",
    "verticleInstances": <num-of-verticle-instances>,
    "dataBrokerIP": "<rabbit mq ip>",
    "dataBrokerPort": <port-number>,
    "dataBrokerVhost": "<Vhost-name>",
    "dataBrokerUserName": "<username-for-rmq>",
    "dataBrokerPassword": "<password-for-rmq>",
    "dataBrokerManagementPort": <time-in-milliseconds>,
    "connectionTimeout": <time-in-milliseconds>,
    "requestedHeartbeat": <time-in-milliseconds>,
    "handshakeTimeout": <time-in-milliseconds>,
    "requestedChannelMax": <time-in-milliseconds>,
    "networkRecoveryInterval": <time-in-milliseconds>,
    "automaticRecoveryEnabled": "true"
}
```

## Setting up ImmuDB

- Refer to the docker files available [here](https://github.com/datakaveri/iudx-deployment/blob/master/Docker-Swarm-deployment/single-node/immudb) to setup > ImmuDB.
- Refer [this](https://github.com/datakaveri/iudx-deployment/blob/master/Docker-Swarm-deployment/single-node/immudb/docker/immudb-config-generator/immudb-config-generator.py) to create table/user.
- In order to connect to the appropriate ImmuDB database, required information such as meteringDatabaseIP,meteringDatabasePort etc. should be updated in the MeteringVerticle module available in [config-example.json](configs/config-example.json).

```
{
    "id": "iudx.data.ingestion.server.databroker.DataBrokerVerticle",
    "verticleInstances": <num-of-verticle-instances>,
    "meteringDatabaseIP": "localhost",
    "meteringDatabasePort": <port-number>,
    "meteringDatabaseName": <database-name>,
    "meteringDatabaseUserName": <username-for-immudb>,
    "meteringDatabasePassword": <password-for-immudb>,
    "meteringDatabaseTableName": <table-name-for-immudb>,
    "meteringPoolSize": <pool-size>
}
```

**Metering Table Schema**

```sql
CREATE TABLE IF NOT EXISTS rsaudit (
    id VARCHAR[128] NOT NULL, 
    api VARCHAR[128], 
    userid VARCHAR[128],
    epochtime INTEGER,
    resourceid VARCHAR[200],
    isotime VARCHAR[128],
    providerid VARCHAR[128],
    PRIMARY KEY id
);
```
