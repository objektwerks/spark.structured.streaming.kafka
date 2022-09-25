name := "spark.structured.streaming.kafka"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.9"
libraryDependencies ++= {
  val sparkVersion = "3.3.0"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.2.0",
    "com.typesafe" % "config" % "1.4.2"
  )
}
