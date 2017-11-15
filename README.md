# Message Stream Processing

## Overview

This demo shows how to process, enrich and route messages to different sinks using Spring Cloud Dataflows (SCDF):

https://docs.spring.io/spring-cloud-dataflow/docs/current-SNAPSHOT/reference/htmlsingle/#getting-started-system-requirements

In this sample we will demonstrate:

1. Streams to capture data
2. Custom components in a stream to transform and enrich messages
3. Routing of messages to different sinks using named destinations
4. Some of the Sinks SCDF offers

### High Level Message Flow

Messages are produced on the left and are sent to sinks on the right.

![alt text](dataflow.png "Flow Of Data")

This demo is built to run on PWS (Pivotal Web Services). This is a Pivotal managed installation of Pivotal Cloud Foundry on AWS.

The components involved (including SCDF itself), and where they are deployed within the Cloud are as follows.

![alt text](components.png "Components")

## Set Up

### Spring Cloud Data Flow Server

Data flows in Streams. SCDF provides a server to create and manage these streams.

To set up the SCDF Server, I modified the scripts found in this project:

https://github.com/lshannon/spring-cloud-data-flow-setup

Similar scripts can be found in the 'scripts' folder of this project.

To run this you will need a paid subscription on PWS and a CloudAMQP plan that is at least as robust as 'Tiger' as this sample heavily leverages Rabbit MQ. There is a month cost for such a plan:

https://console.run.pivotal.io/marketplace/services/3ba9445c-c709-4153-a343-e4ff5807316a

```shell

cf create-service cloudamqp tiger rabbit-scdf-queue

```

As this sample leverages some of the useful behaviors in the SCDF Rabbit sources, sinks and designated channels, **this will only work with Rabbit MQ as the backing data bus**. 

### Message Production

Our messages will be unstructured texts messages being published on to Rabbit MQ queue. They will come as a steady stream.

The application that produces them is in the 'simple-message-producer' folder. This is a simple Spring Boot application that will write messages to a Rabbit MQ Exchange upon start up.

To compile this you will need a RabbitMQ running locally (see below). Compilation can be done using the maven wrapper at the root of the application folder.

```shell

./mvnw clean package

```
Also in the root of the application folder is a manifest.yml file for deployment to PCF.

```yaml

---
applications:
- name: simple-message-producer
  random-route: true
  memory: 1G
  instances: 1
  path: target/simple-message-producer.jar
  services:
   - scdf-rabbitmq-queue

```
**NOTE:** It is bound to the same Rabbit MQ service SCDF is using. Do not change this as extra configuration is required to get SCDF and a step within a stream to use different instances of the same messaging service:

If you are using multiple brokers (ie: Rabbit MQ and Kafka or two Rabbit MQ), there is a bit of extra configuration.

https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#spring-cloud-dataflow-stream-multi-binder

To keep things simple for this demo we will be using one Rabbit MQ service for everything.


#### Setting Up RabbitMQ Locally (Only if you wish to build the message-producer)

To build the 'simple-message-producer' locally you will need a RabbitMQ running locally, otherwise the Test will not pass as the RabbitTemplate will not be able to create a ConnectionFacactory. With a Mac installing Rabbit can be done using Brew:

```shell

brew install rabbitmq

...

brew services start rabbitmq

```
After the installation, admin console can be found here:

http://127.0.0.1:15672/

(guest/guest)

The message-producer application is configured to create the Exchanges and Queues it needs upon start up. A Fan Out exchange called 'messages' will be created, a Queue also called 'messages' (lazy with the naming) is created. The Exchange is bound to the Queue. These details can be found in the MessageQueueConfig class of the 'simple-message-producer'.


#### Setting Up RabbitMQ on PWS (Required to run the sample)

Simply create a free instance of the CloudAMQP broker from the Marketplace:

```shell

cf create-service cloudamqp tiger scdf-rabbitmq-queue

```
Similar to local, once the application is connected it will create the necessary queues and exchanges.

Part of the Service in creating the Rabbit MQ Service is to create the manager.

![alt text](rabbit-manager-view.png "Rabbit Manager")

This is a useful interface to refer too. Here you can see where messages are going and what Exchanges and Queues are created.


The 'simple-message-producer' and SCDF itself will create the Rabbit Exchanges and Queues it required at run time.

No configuration of the Rabbit Service is required for this demo.

### Connecting To SCDF via The Shell

From within the 'script' folder of the run the following java command to launch a SCDF Shell Session.

```shell

➜  shell git:(master) ✗ pwd
/Users/lshannon/Documents/message-stream-processing/scripts/shell
➜  shell git:(master) ✗ java -jar spring-cloud-dataflow-shell-1.2.3.RELEASE.jar
  ____                              ____ _                __
 / ___| _ __  _ __(_)_ __   __ _   / ___| | ___  _   _  __| |
 \___ \| '_ \| '__| | '_ \ / _` | | |   | |/ _ \| | | |/ _` |
  ___) | |_) | |  | | | | | (_| | | |___| | (_) | |_| | (_| |
 |____/| .__/|_|  |_|_| |_|\__, |  \____|_|\___/ \__,_|\__,_|
  ____ |_|    _          __|___/                 __________
 |  _ \  __ _| |_ __ _  |  ___| | _____      __  \ \ \ \ \ \
 | | | |/ _` | __/ _` | | |_  | |/ _ \ \ /\ / /   \ \ \ \ \ \
 | |_| | (_| | || (_| | |  _| | | (_) \ V  V /    / / / / / /
 |____/ \__,_|\__\__,_| |_|   |_|\___/ \_/\_/    /_/_/_/_/_/

1.2.3.RELEASE

Welcome to the Spring Cloud Data Flow shell. For assistance hit TAB or type "help".
server-unknown:>

```
Next we will connect to the SCDF server in the previous step. Your server name will be based on your Org and Space.

```shell

dataflow config server https://<scdf server route>.cfapps.io

```
Next we can load all of the Sources, Sinks and Transformers SCDF provides out of the box.

```shell

dataflow:>app import http://bit.ly/Bacon-RELEASE-stream-applications-rabbit-maven
Successfully registered 60 applications from [source.sftp, source.file.metadata, processor.tcp-client, source.s3.metadata, source.jms, source.ftp, processor.transform.metadata, source.time, sink.s3.metadata, sink.log, processor.scriptable-transform, source.load-generator, sink.websocket.metadata, source.syslog, processor.transform, sink.task-launcher-local.metadata, source.loggregator.metadata, source.s3, source.load-generator.metadata, processor.pmml.metadata, source.loggregator, source.tcp.metadata, processor.httpclient.metadata, sink.file.metadata, source.triggertask, source.twitterstream, source.gemfire-cq.metadata, processor.aggregator.metadata, source.mongodb, source.time.metadata, sink.counter.metadata, source.gemfire-cq, source.http, sink.tcp.metadata, sink.pgcopy.metadata, source.rabbit, sink.task-launcher-yarn, source.jms.metadata, sink.gemfire.metadata, sink.cassandra.metadata, processor.tcp-client.metadata, sink.throughput, processor.header-enricher, sink.task-launcher-local, sink.aggregate-counter.metadata, sink.mongodb, sink.log.metadata, processor.splitter, sink.hdfs-dataset, source.tcp, source.trigger, source.mongodb.metadata, processor.bridge, source.http.metadata, sink.ftp, source.rabbit.metadata, sink.jdbc, source.jdbc.metadata, sink.rabbit.metadata, sink.aggregate-counter, processor.pmml, sink.router.metadata, sink.cassandra, source.tcp-client.metadata, processor.filter.metadata, processor.groovy-transform, processor.header-enricher.metadata, source.ftp.metadata, sink.router, sink.redis-pubsub, source.tcp-client, processor.httpclient, sink.file, sink.websocket, sink.s3, source.syslog.metadata, sink.rabbit, sink.counter, sink.gpfdist.metadata, source.mail.metadata, source.trigger.metadata, processor.filter, sink.pgcopy, sink.jdbc.metadata, sink.gpfdist, sink.ftp.metadata, processor.splitter.metadata, sink.sftp, sink.field-value-counter, processor.groovy-filter.metadata, source.triggertask.metadata, sink.hdfs, processor.groovy-filter, sink.redis-pubsub.metadata, source.sftp.metadata, sink.field-value-counter.metadata, processor.bridge.metadata, processor.groovy-transform.metadata, processor.aggregator, sink.sftp.metadata, sink.throughput.metadata, sink.hdfs-dataset.metadata, sink.tcp, sink.task-launcher-cloudfoundry.metadata, source.mail, source.gemfire.metadata, source.jdbc, sink.task-launcher-yarn.metadata, sink.gemfire, source.gemfire, sink.hdfs.metadata, source.twitterstream.metadata, processor.tasklaunchrequest-transform, sink.task-launcher-cloudfoundry, source.file, sink.mongodb.metadata, processor.tasklaunchrequest-transform.metadata, processor.scriptable-transform.metadata]

```
Running a 'app list' in the SCDF shell will show everything we just imported. We can also see it in the dashboard view of the SCDF Server:

https://<scdf server route>.cfapps.io/dashboard/index.html#/apps/apps
	
![alt text](scdf-app-list.png "Rabbit Manager")

## Transforming The Message

Our unstructured text message will be transformed to JSON and enriched with data to route it.

To do this we will need to:

1. Create a Processor class with a Stream Listener to specifiy the input and output
2. Register the component with the SCDF Data Server running on PWS

To register the component we will need to host it somewhere. This is where the 'processor-repository' comes in. More on this later.

### Simple Message Processor

This simple Spring Boot application is going to grab the messages from Rabbit MQ that the 'simple-message-producer' writes and transform and enrich them.



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

2017-11-13T18:20:55.76-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.762  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"Do or do not, there is not try","routingKey":".net"}
   2017-11-13T18:20:55.76-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.763  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"This is Sparta!","routingKey":"java"}
   2017-11-13T18:20:55.76-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.764  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"Could you pass the salt?","routingKey":"db"}
   2017-11-13T18:20:55.76-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.765  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"I'm pickle Rick!","routingKey":"file"}
   2017-11-13T18:20:55.76-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.768  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"By the power of Grey Skull!","routingKey":".net"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.769  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"Do or do not, there is not try","routingKey":"log"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.770  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"This is Sparta!","routingKey":"db"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.773  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"By the power of Grey Skull!","routingKey":".net"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.774  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"Shop smart, shop S-mart","routingKey":"db"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.775  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"Do or do not, there is not try","routingKey":"file"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:20:55.777  INFO 13 --- [ocessor.luke1-1] opment-dataflow-server-VQZn5fj-luke1-log : {"message":"I'm pickle Rick!","routingKey":"java"}
   2017-11-13T18:20:55.77-0500 [APP/PROC/WEB/0] OUT 2017-11-13 23:


```

## Consuming the Routed Streams

Lets start routing these messages. To do this we will set up the consumers first, in doing so we will set up the Rabbit MQ exchanges and queues to handle the messages we route.

This is done in SCDF using named destinations:

https://docs.spring.io/spring-cloud-dataflow/docs/1.2.3.RELEASE/reference/htmlsingle/#spring-cloud-dataflow-stream-dsl-named-destinations

For each routingKey that our transformer will add, we will create a named destination. This will generate a Queue and Exchange.

These are created by running the following commands in the SCDF shell:

```shell

stream create --name j123 --definition ":java > log" --deploy
stream create --name n123 --definition ":net > log" --deploy
stream create --name l123 --definition ":log > log" --deploy
stream create --name d123 --definition ":db > log" --deploy
stream create --name f123 --definition ":file > log" --deploy

```


## Routing Messages

Next we will route each of the messages to its own rabbit queue.

Do do this we will use the rabbit sink of Spring Cloud Data Flow and the 'routingKey' field we added to the message with the 'simple-message-processor'.

```shell

stream create lws1 --definition "r1: rabbit --queues=messages | simple-message-processor | r2: rabbit --routing-key-expression=#jsonPath(payload,'$.routingKey') --exchange-expression=#jsonPath(payload,'$.routingKey')" --deploy

````
The result of this stream will be to create a queue in Rabbit for each unique 'routingKey' and then send the messages to the queue.


