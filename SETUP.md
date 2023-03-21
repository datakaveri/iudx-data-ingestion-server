Setup Guide
----

This document contains the installation and configuration processes of the external modules of each Verticle in IUDX Data Ingestion Pipeline Server.

<p align="center">
<img src="docs/di_server_overview.png">
</p>


Data Ingestion Pipeline server connects with various external dependencies namely :

 - `RabbitMQ` : used to publish and subscribe different types of messages or events.
 
 
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
    "prodVhost": "<prodVhost-name>",
    "internalVhost": "<internalVhost-name>",
    "externalVhost": "<externalVhost-name>",
    
}
```

## Setting up Auditing(Metering)
```
{
    "id": "iudx.data.ingestion.server.metering.MeteringVerticle",
    "verticleInstances": <num-of-verticle-instances>,
}
```

**Metering Data Process**
```
In order to store metering data it needs to be published into RMQ and make sure 
"auditing_rs" table must exist in respective database schema.
```

