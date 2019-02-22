package streaming

import org.apache.spark.sql.types.{StringType, StructType}
import org.apache.spark.sql.{Encoders, ForeachWriter}

case class KeyValue(key: String, value: String)

object KeyValue {
  val keyValueSchema = Encoders.product[KeyValue].schema
  val keyValueStructType = new StructType()
    .add(name = "key", dataType = StringType, nullable = false)
    .add(name = "value", dataType = StringType, nullable = false)
  val keyValueForeachWriter = new ForeachWriter[KeyValue] {
    override def open(partitionId: Long, version: Long): Boolean = true
    override def process(keyValue: KeyValue): Unit = println(s"$keyValue")
    override def close(errorOrNull: Throwable): Unit = ()
  }
}