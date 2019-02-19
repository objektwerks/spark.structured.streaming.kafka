package streaming

import org.scalatest.FunSuite

class KafkaSparkStructuredStreamingTest extends FunSuite {
  import SparkInstance._
  import sparkSession.implicits._
  import streaming.KeyValue._

  val (kafkaBootstrapServers, urls) = ("kafka.bootstrap.servers", "localhost:9092")
  val sourceTopic = "source-topic"
  val sinkTopic = "sink-topic"

  test("json") {
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
      .as[KeyValue]
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
      .schema(keyValueStructType)
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sourceTopic)
      .load
      .as[KeyValue]
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
      .schema(keyValueStructType)
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sinkTopic)
      .load
      .as[KeyValue]
      .writeStream
      .foreach(keyValueForeachWriter)
      .start
      .awaitTermination(3000L)
  }
}