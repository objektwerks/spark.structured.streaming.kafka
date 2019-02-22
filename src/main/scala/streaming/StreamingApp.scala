package streaming

import org.apache.spark.sql.streaming.OutputMode

import SparkInstance._
import KeyValue._

object StreamingApp extends App {
  val (kafkaBootstrapServers, urls) = ("kafka.bootstrap.servers", "localhost:9092")
  val sourceTopic = "source-topic"
  val sinkTopic = "sink-topic"
  val consoleQuery = sparkSession
    .readStream
    .format("kafka")
    .option(kafkaBootstrapServers, urls)
    .option("subscribe", s"$sourceTopic,$sinkTopic")
    .load
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "CAST(topic AS STRING)")
    .writeStream
    .outputMode(OutputMode.Append)
    .format("console")
    .start

  val jsonToSourceTopic = sparkSession
    .readStream
    .option("basePath", "./data/keyvalue")
    .schema(keyValueStructType)
    .json("./data/keyvalue")
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .writeStream
    .format("kafka")
    .option(kafkaBootstrapServers, urls)
    .option("topic", sourceTopic)
    .option("checkpointLocation", "./target/source-topic")
    .start

  import org.apache.spark.sql.functions._

  val sourceTopicToSinkTopic = sparkSession
    .readStream
    .format("kafka")
    .option(kafkaBootstrapServers, urls)
    .option("subscribe", sourceTopic)
    .load
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .withColumn("value", upper(col("value")))
    .writeStream
    .format("kafka")
    .option(kafkaBootstrapServers, urls)
    .option("topic", sinkTopic)
    .option("checkpointLocation", "./target/sink-topic")
    .start

  sys.addShutdownHook {
    println("Terminating KafkaSparkStructuredStreamingApp ...")
    jsonToSourceTopic.awaitTermination(3000L)
    sourceTopicToSinkTopic.awaitTermination(3000L)
    consoleQuery.awaitTermination(6000L)
    ()
  }
}