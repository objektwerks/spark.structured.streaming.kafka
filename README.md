Spark Structured Streaming Kafka
--------------------------------
>The purpose of this project is to test Kafka with Spark Structured Streaming.

Homebrew
--------
>Install Homebrew on OSX.

Installation
------------
>Install the following packages via Homebrew:

1. brew tap homebrew/services
2. brew install kafka  ( which installs zookeeper )

Service
-------
>Start:

1. brew services start zookeeper
2. brew services start kafka

>Stop:

1. brew services stop kafka
2. brew services stop zookeeper

Run
---
1. sbt clean compile run

>**WARNING** The Homebrew install of apache-spark is configured for Scala 2.11, **NOT** Scala 2.12.
>Consequently, the build.sbt is set to Scala 2.11.12. The alternative would be to download and install
>the Scala 2.12 version of Apache Spark via the Apache Spart web site.

>**SBT 1.3.0*** ClassLoader management: To prevent resource leaks, sbt 1.3.0 closes the ephemeral ClassLoaders
>used by the run and test tasks after those tasks complete. This may cause downstream crashes if the task uses
>ShutdownHooks or if any threads created by the tasks continue running after the task completes. To disable this
>behavior, run sbt with **-Dsbt.classloader.close=false**.

Submit
------
>First create a log4j.properties file from log4j.properties.template.
>See: /usr/local/Cellar/apache-spark/2.4.3/libexec/conf/log4j.properties.template

1. sbt clean compile package
2. chmod +x submit.sh ( required only once )
3. ./submit.sh

>WARNING: Requires correct Scala version vis-a-vis Spark version to run correctly.

UI
--
1. SparkUI : localhost:4040
2. History Server UI : localhost:18080 : start-history-server.sh | stop-history-server.sh

Stop
----
1. Control-C
 
Log
---
1. ./target/app.log

Events
------
1. /tmp/spark-events

Kafka
-----
>Topics 1) source-topic and 2) sink-topic

* kafka-topics --zookeeper localhost:2181 --list
* kafka-topics --zookeeper localhost:2181 --describe --topic source-topic
* kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic source-topic --time -1
* kafka-consumer-groups --bootstrap-server localhost:9092 --group objektwerks-group --describe
* kafka-topics --zookeeper localhost:2181 --delete --topic source-topic
* kafka-consumer-groups --bootstrap-server localhost:9092 --list
* kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group objektwerks-group
