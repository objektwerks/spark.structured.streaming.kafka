package streaming

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{StringType, StructType}

object KafkaStructuredStreamingApp {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load("app.conf").getConfig("app")
    val (kafkaBootstrapServers, urls) = ("kafka.bootstrap.servers", conf.getString("kafka-bootstrap-servers"))
    val sourceTopic = conf.getString("source-topic")
    val sinkTopic = conf.getString("sink-topic")

    val sparkEventLogDir = conf.getString("spark.eventLog.dir")
    val sparkEventDirCreated = createSparkEventsDir(sparkEventLogDir)
    println(s"*** $sparkEventLogDir exists or was created: $sparkEventDirCreated")

    val keyValueStructType = new StructType()
      .add(name = "key", dataType = StringType, nullable = false)
      .add(name = "value", dataType = StringType, nullable = false)

    val sparkSession = SparkSession.builder
      .master(conf.getString("master"))
      .appName(conf.getString("name"))
      .config("spark.eventLog.enabled", conf.getBoolean("spark.eventLog.enabled"))
      .config("spark.eventLog.dir", sparkEventLogDir)
      .getOrCreate()
    println("*** Initialized Spark KafkaStructuredStreamingApp. Press Ctrl C to terminate.")

    sys.addShutdownHook {
      sparkSession.stop
      println("*** Terminated Spark KafkaStructuredStreamingApp.")
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

    consoleQuery.awaitTermination
    jsonToSourceTopic.awaitTermination
    sourceTopicToSinkTopic.awaitTermination
  }
}