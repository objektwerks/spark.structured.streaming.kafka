name := "kafka.spark.structured.streaming"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.8"
libraryDependencies ++= {
  val sparkVersion = "2.4.0"
  Seq(
    "org.apache.kafka" %% "kafka" % "2.1.0",
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )
}
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8"