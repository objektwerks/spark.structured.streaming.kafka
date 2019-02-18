package kafka

import java.util.Properties

import scala.io.Source

object KafkaCommon {
  val kafkaConsumerProperties = loadProperties("/kafka.consumer.properties")
  val kafkaProducerProperties = loadProperties("/kafka.producer.properties")
  val sourceTopic = "source-topic"
  val sinkTopic = "sink-topic"

  def loadProperties(file: String): Properties = {
    val properties = new Properties()
    properties.load(Source.fromInputStream(getClass.getResourceAsStream(file)).bufferedReader())
    properties
  }
}