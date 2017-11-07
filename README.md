# Message Stream Processing

## Overview

This demo shows how to process, enrich and route messages to different sinks.

![alt text](dataflow.png "Flow Of Data")

This demo is built on PWS (Pivotal Web Services). This is a Pivotal managed installation of Pivotal Cloud Foundry on AWS.

The components involved, and where they are deployed within the Cloud are as follows.

![alt text](components.png "Components")

## Set Up

### Spring Cloud Data Flow Server
To set up the Spring Cloud Data Flow (SCDF) Server, please follow the steps in this repo:
https://github.com/lshannon/spring-cloud-data-flow-setup

We will use the Server to create and manage streams.

### Message Production

Our messages will be SOAP messages being published on to Rabbit MQ queue. They will come as a steady stream. To get this result we took the following code base and made a few tweaks for it to write its SOAP Objects into a RabbitMQ exchange:
https://spring.io/guides/gs/producing-web-service/

#### Setting Up RabbitMQ Locally

To build this code locally you will need a RabbitMQ running locally, otherwise the Test will not pass as the RabbitTemplate will not be able to create a ConnectionFacactory. With a Mac installing Rabbit can be done using Brew:

```shell

brew install rabbitmq
...
brew services start rabbitmq

```
After the installation, admin console can be found here:
http://127.0.0.1:15672/
(guest/guest)

The way this Spring Boot application is configured, a Fan Out exchange called 'messages' will be created, a Queue also called 'messages' (lazy with the naming) and the Exchange bound to the Queue. These details can be found in the MessageQueueConfig class.

#### Setting Up RabbitMQ on PWS

Simply create a free instance of the CloudAMQP broker from the Marketplace:

```shell

cf create-service cloudamqp lemur messages

```
Similar to local, once the application is connected it will create the necessary queues and exchanges.

## Transforming From SOAP to JSON

To do this we will need to:

1. Create a Processor class with a Stream Listener to specifiy the input and output
2. Install this application into the local maven repository (a remote one can be used as well)
3. Register the component with the SCDF Data Server running on PWS

The Input is going to be the Rabbit MQ our message production application is posting messages too.

The Output is a different Rabbit MQ. Specifically the one the SCDF streams are using as a backing data bus. Kafka can also be used here, however PWS does not have a Kafka service. So Rabbit it is.

### Installing The Transformer

The are a few ways to get the binary for the component on to the server. For this example the jar has been added to github. The link to this binary can be used in the URI.

The following registers the component in SCDF's Data Server running in PWS.

```shell

dataflow:>app register --name soap-to-json-transformer --type processor --uri https://github.com/lshannon/message-stream-processing/blob/master/soap-to-json-transformer-0.1.0.jar
Successfully registered application 'processor:soap-to-json-transformer'


```
For more on registering components:
https://docs.spring.io/spring-cloud-dataflow/docs/1.2.1.RELEASE/reference/html/spring-cloud-dataflow-register-apps.html

### Consuming The Message Into The Custom Processor

To get the messages being produced to the RabbitMQ Queue, create the following stream:

```shell

stream create message-ingest --definition "rabbit --queues=messages | soap-to-json-transformer | log" --deploy

```

## Routing Messages

## Configuring Routing Behavior


