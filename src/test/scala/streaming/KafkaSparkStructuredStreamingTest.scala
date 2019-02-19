package streaming

import org.scalatest.FunSuite

class KafkaSparkStructuredStreamingTest extends FunSuite {
  import SparkInstance._
  import sparkSession.implicits._
  import streaming.KeyValue._

  val (kafkaBootstrapServers, urls) = ("kafka.bootstrap.servers", "localhost:9092")
  val sourceTopic = "source-topic"
  val sinkTopic = "sink-topic"

  test("source") {
    sparkSession
      .readStream
      .option("basePath", "./data/keyvalue")
      .schema(keyValueSchema)
      .json("./data/keyvalue")
      .as[KeyValue]
      .writeStream
      .foreach(keyValueForeachWriter)
      .start
      .awaitTermination(3000L)
  }

  test("source > sink") {
    sparkSession
      .readStream
      .option("basePath", "./data/keyvalue")
      .schema(keyValueStructType)
      .json("./data/keyvalue")
      .select("key", "value")
      .writeStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("topic", sourceTopic)
      .option("checkpointLocation", "./target/source-topic")
      .start
      .awaitTermination(3000L)

    sparkSession
      .readStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sourceTopic)
      .option("startingOffsets", "earliest")
      .load
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .writeStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("topic", sinkTopic)
      .option("checkpointLocation", "./target/sink-topic")
      .start
      .awaitTermination(3000L)

    sparkSession
      .readStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sinkTopic)
      .option("startingOffsets", "earliest")
      .load
      .writeStream
      .format("console")
      .start
      .awaitTermination(3000L)
  }
}