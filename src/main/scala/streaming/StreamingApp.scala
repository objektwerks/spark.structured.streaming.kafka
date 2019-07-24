package streaming

import java.net.InetAddress

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{StringType, StructType}

object StreamingApp extends App {
  val conf = ConfigFactory.load("streaming.conf").getConfig("streaming")
  val (kafkaBootstrapServers, urls) = ("kafka.bootstrap.servers", conf.getString("kafka-bootstrap-servers"))
  val sourceTopic = conf.getString("source-topic")
  val sinkTopic = conf.getString("sink-topic")

  val keyValueStructType = new StructType()
    .add(name = "key", dataType = StringType, nullable = false)
    .add(name = "value", dataType = StringType, nullable = false)

  val sparkSession = SparkSession.builder
    .master("local[*]")
    .appName(InetAddress.getLocalHost.getHostName)
    .config("spark.eventLog.enabled", true)
    .config("spark.eventLog.dir", "/tmp/spark-events")
    .getOrCreate()
  println("Initialized Spark StreamingJob. Press Ctrl C to terminate.")

  sys.addShutdownHook {
    sparkSession.stop()
    println("Terminated Spark StreamingJob.")
  }

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
    .option("basePath", conf.getString("key-value-json-path"))
    .schema(keyValueStructType)
    .json(conf.getString("key-value-json-path"))
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .writeStream
    .format("kafka")
    .option(kafkaBootstrapServers, urls)
    .option("topic", sourceTopic)
    .option("checkpointLocation", conf.getString("source-topic-checkpoint-location"))
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
    .option("checkpointLocation", conf.getString("sink-topic-checkpoint-location"))
    .start

  jsonToSourceTopic.awaitTermination()
  sourceTopicToSinkTopic.awaitTermination()
  consoleQuery.awaitTermination()
}