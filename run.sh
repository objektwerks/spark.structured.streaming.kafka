#!/bin/sh
spark-submit \
  --class streaming.KafkaStructuredStreamingApp \
  --master local[2] \
  --packages org.apache.spark:spark-sql-kafka-0-10:2.4.3,com.typesafe:config:1.3.4 \
  ./target/scala-2.12/kafka-spark-structured-streaming_2.12-0.1.jar