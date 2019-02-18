package streaming

import org.apache.spark.sql.Encoders

case class KeyValue(key: String, value: String)

object KeyValue {
  val keyValueSchema = Encoders.product[KeyValue].schema
}