package streaming

import org.scalatest.FunSuite
import streaming.KeyValue._
import streaming.SparkInstance._
import streaming.SparkInstance.sparkSession.implicits._

class KafkaSparkStructuredStreamingTest extends FunSuite {
  val sourceTopic = "source-topic"
  val sinkTopic = "sink-topic"

  test("json") {
    sparkSession
      .readStream
      .option("basePath", "./data/keyvalue")
      .schema(keyValueStructType)
      .json("./data/keyvalue")
      .as[KeyValue]
      .writeStream
      .foreach(keyValueForeachWriter)
      .start
      .awaitTermination
  }

  test("source > sink") {
    sparkSession
      .readStream
      .option("basePath", "./data")
      .schema(keyValueSchema)
      .json("./data")
      .as[KeyValue]
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", sourceTopic)
      .option("checkpointLocation", "./target/cpdir")
      .start
      .awaitTermination
    sparkSession
      .readStream
      .format("kafka")
      .schema(KeyValue.keyValueSchema)
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
      .awaitTermination
    sparkSession
      .readStream
      .format("kafka")
      .schema(KeyValue.keyValueSchema)
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", sinkTopic)
      .load
      .as[KeyValue]
      .writeStream
      .format("console")
      .start
      .awaitTermination
  }
}