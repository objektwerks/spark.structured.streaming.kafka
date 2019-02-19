package streaming

import org.scalatest.FunSuite

class KafkaSparkStructuredStreamingTest extends FunSuite {
  import SparkInstance._
  import sparkSession.implicits._
  import streaming.KeyValue._

  val (kafkaBootstrapServers, urls) = ("kafka.bootstrap.servers", "localhost:9092")
  val sourceTopic = "source-topic"
  val sinkTopic = "sink-topic"

  test("verify json") {
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

  test("read json > write source topic") {
    sparkSession
      .readStream
      .option("basePath", "./data/keyvalue")
      .schema(keyValueStructType)
      .json("./data/keyvalue")
      .selectExpr("CAST(key AS STRING) AS key", "to_json(struct(*)) AS value")
      .writeStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("topic", sourceTopic)
      .option("checkpointLocation", "./target/source-topic")
      .start
      .awaitTermination(3000L)
  }

  test("verify source topic") {
    sparkSession
      .readStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sourceTopic)
      .load
      .writeStream
      .format("console")
      .start
      .awaitTermination(3000L)
  }

  test("read source topic > write sink topic") {
    sparkSession
      .readStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sourceTopic)
      .load
      .selectExpr("CAST(key AS STRING) AS key", "to_json(struct(*)) AS value")
      .writeStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("topic", sinkTopic)
      .option("checkpointLocation", "./target/sink-topic")
      .start
      .awaitTermination(3000L)
  }

  test("verify sink topic") {
    sparkSession
      .readStream
      .format("kafka")
      .option(kafkaBootstrapServers, urls)
      .option("subscribe", sinkTopic)
      .load
      .writeStream
      .format("console")
      .start
      .awaitTermination(3000L)
  }
}