# Message Stream Processing

## Overview

This demo shows how to process, enrich and route messages to different sinks.

![alt text](dataflow.png "Flow Of Data")

This demo is built on PWS (Pivotal Web Services). This is a Pivotal managed installation of Pivotal Cloud Foundry on AWS.

The components involved, and where they are deployed within the Cloud are as follows.

![alt text](components.png "Components")

## Set Up

### Spring Cloud Data Flow Server

To set up the Spring Cloud Data Flow (SCDF) Server, I modified the scripts found in this project:

https://github.com/lshannon/spring-cloud-data-flow-setup

Similar scripts can be found in the 'scripts' folder of this project.

To run this you will need a paid subscription on PWS and a CloudAMQP plan that is at least as robust as 'Tiger'.

### Message Production

Our messages will be SOAP messages being published on to Rabbit MQ queue. They will come as a steady stream. To get this result we took the following code base and made a few tweaks for it to write its SOAP Objects into a RabbitMQ exchange:

https://spring.io/guides/gs/producing-web-service/

#### Setting Up RabbitMQ Locally (Only if you wish to build the message-producer)

To build the message-producer locally you will need a RabbitMQ running locally, otherwise the Test will not pass as the RabbitTemplate will not be able to create a ConnectionFacactory. With a Mac installing Rabbit can be done using Brew:

```shell

brew install rabbitmq

...

brew services start rabbitmq

```
After the installation, admin console can be found here:

http://127.0.0.1:15672/

(guest/guest)

The message-producer application is configured to create the Exchanges and Queues it needs upon start up. A Fan Out exchange called 'messages' will be created, a Queue also called 'messages' (lazy with the naming) is created. The Exchange is bound to the Queue. These details can be found in the MessageQueueConfig class of the message-producer.

#### Setting Up RabbitMQ on PWS

Simply create a free instance of the CloudAMQP broker from the Marketplace:

```shell

cf create-service cloudamqp tiger scdf-rabbitmq-queue

```
Similar to local, once the application is connected it will create the necessary queues and exchanges.

We will use this for both the data bus of SCDF and our queue for our SOAP messages.

Lets test by creating a stream that writes the messages in the RabbitMQ 'messages' queue to the log.

```shell

dataflow config server https://message-routingdevelopment-dataflow-server.cfapps.io
app import http://bit.ly/Bacon-RELEASE-stream-applications-rabbit-maven
stream create luke1 --definition "rabbit --queues=messages | log" --deploy

```
A note routes need be less than 63 characters. Ensure not to put larger Stream names as SCDF will add to them.

## Transforming From SOAP to JSON

To do this we will need to:

1. Create a Processor class with a Stream Listener to specifiy the input and output
2. Install this application into the local maven repository (a remote one can be used as well)
3. Register the component with the SCDF Data Server running on PWS

The Input is going to be the Rabbit MQ our message production application is posting messages too.

The Output is a different Rabbit MQ. Specifically the one the SCDF streams are using as a backing data bus. Kafka can also be used here, however PWS does not have a Kafka service. So Rabbit it is.

A note on Queues in Spring Cloud Data Flow. If you are using multiple brokers (ie: Rabbit MQ and Kafka or two Rabbit MQ), there is a bit of extra configuration.

https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#spring-cloud-dataflow-stream-multi-binder

To make our lives simpler we will be using the same RabbitMQ instance for our message-producer to write too as well as SCDF to use as a backing message bus.

### Installing The Transformer

The transformer code is located in the 'simple-message-processor' project. This custom component performs:

1. Transform message to JSON
2. Adding routing key to JSON

This can be found in the Process component.

```java

  @PostConstruct
	private void init() {
		routingKeys = new ArrayList<String>();
		routingKeys.add("java");
		routingKeys.add(".net");
		routingKeys.add("log");
		routingKeys.add("db");
		routingKeys.add("file");
		mapper = new ObjectMapper();
	}

	@StreamListener(Processor.INPUT)
	@Output(Processor.OUTPUT)
	public String process(String message) {
		ProcessedMessage messageObj = new ProcessedMessage(message, getRoutingKey());
		String json = null;
		log.debug("Got a message to process: " + message);
		try {
			json = mapper.writeValueAsString(messageObj);
		} catch (JsonProcessingException e) {
			log.error("Unable to convert: " + messageObj.toString() + " to JSON");
		}
		log.debug("Processing Complete. Resulting JSON: " + json);
		return json;
	}

	private String getRoutingKey() {
		int rnd = new Random().nextInt(routingKeys.size());
		return routingKeys.get(rnd);
	}


```

This application is built and packaged using maven. Part of the build process is to copy this artifact to the 'processor-repository' static folder.

From inside the folder run:

```shell

./mvnw clean package

```

You will see the following line where the copy is happening.

```shell

[copy] Copying 1 file to /Users/lshannon/Documents/message-stream-processing/processor-repository/src/main/resources/static

```
To compile the 'processor-repository' run the maven clean package in the 'processor-repository'.

```shell

./mvnw clean package

```
While in this folder, log into PCF and then run a cf push.

Once the 'processor-repository' is running in PWS, hit the root page of the application to get the links for the processor and groovy routing rules (routing explained below).

For a more robust solution for managing custom modules, Spring Cloud Skipper should be considered:

https://github.com/spring-cloud/spring-cloud-skipper

Once the 'processor-repository' is running in PWS, the custom component can be registered in the SCDF shell with the following command.

```shell

dataflow:>app register --name simple-message-processor --type processor --uri https://<application name>.cfapps.io/simple-message-processor.jar
Successfully registered application 'processor:simple-message-processor'

```
Should you wish to remove it, in the SCDF shell run the following:

```shell

app unregister --name simple-message-processor --type processor

```

For more on registering components:
https://docs.spring.io/spring-cloud-dataflow/docs/1.2.1.RELEASE/reference/html/spring-cloud-dataflow-register-apps.html

### Consuming The Message Into The Custom Processor

To test the custom processor, create a stream that routes the messages from the 'simple-message-producer' through the processor and into the logs.

```shell

stream create processor-test --definition "rabbit --queues=messages | simple-message-processor | log" --deploy

```
If this successfully works the logs will contain the following:

```shell



```

## Routing Messages

Next we will route each of the messages to its own rabbit queue. We will use the Router processor for this.

Do groovy based routing, host the script in the processor-repo:

https://github.com/spring-cloud-stream-app-starters/router/blob/master/spring-cloud-starter-stream-sink-router/README.adoc

```shell


stream create processor-test --definition "rabbit --queues=messages | simple-message-processor | router --script=https://processor-repository-yeastlike-conjugator.cfapps.io/groovy-routing-rules.groovy" --deploy

````

## Consuming the Routed Streams

To consume the routed messages, we need to create a stream for each queue.

For each queue created by the router we will create a stream to consume it and send it to the correct sink.

These are created by running the following commands in the SCDF shell:

```shell

stream create java-consume --definition "rabbit --queues=java | tcp --host=https://javaconsumer/endpoint" --deploy

stream create net-consume --definition "rabbit --queues=net | tcp --host=https://.netconsumer/endpoint" --deploy

stream create log-consume --definition "rabbit --queues=log | log" --deploy

stream create db-consume --definition "rabbit --queues=db | jdbc --password=0W}PMhbn --driver-class-name=sadasdasd --username=asdsad --schema=sadsd --url=asd" --deploy

stream create file-consume -definition "rabbit --queues=file | ftp --filename-expression=asdas --password=sadsadasd --host=ssadd --username=asdsd" --deploy

```


