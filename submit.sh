#!/bin/sh
spark-submit \
  --class streaming.KafkaStructuredStreamingApp \
  --master local[2] \
  --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1,com.typesafe:config:1.4.0 \
  ./target/scala-2.12/kafka-spark-structured-streaming_2.12-0.1-SNAPSHOT.jar