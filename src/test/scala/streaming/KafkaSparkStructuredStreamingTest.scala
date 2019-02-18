package streaming

import org.scalatest.FunSuite

class KafkaSparkStructuredStreamingTest extends FunSuite {
  import SparkInstance._
  import sparkSession.implicits._
  import streaming.KeyValue._

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
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", sourceTopic)
      .option("checkpointLocation", "./target/cpdir")
      .start
      .awaitTermination(3000L)
    sparkSession
      .readStream
      .format("kafka")
      .schema(keyValueStructType)
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", sourceTopic)
      .load
      .as[KeyValue]
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", sinkTopic)
      .option("checkpointLocation", "./target/cpdir")
      .start
      .awaitTermination(3000L)
    sparkSession
      .readStream
      .format("kafka")
      .schema(keyValueStructType)
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", sinkTopic)
      .load
      .as[KeyValue]
      .writeStream
      .foreach(keyValueForeachWriter)
      .start
      .awaitTermination(3000L)
  }
}