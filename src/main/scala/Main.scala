package ckh

import java.net.Socket
import java.io._
import java.nio.ByteBuffer
import java.time._

import utils._
import ckh.native._

class ClickhouseClientException(message: String) extends RuntimeException(message)
class ClickhouseServerException(causal: ServerException) extends RuntimeException {
  override def toString(): String = causal.toString()
}

object Main {
  def main(args: Array[String]): Unit = {
    val client = Client("127.0.0.1", 32769)
    val connection = client.connect()

    // val values = Iterator(Block(
    //     table = None,
    //     BlockInfo.empty,
    //     nbColumns = 1,
    //     nbRows = 1,
    //     columns = List(DateColumn("date", Array(LocalDate.now.plusDays(20))))
    // ))

    val ext = Iterator(Block(
      Some("coucou"),
      BlockInfo.empty,
      1,
      1,
      List(
        Float64Column("ahah", Array(1.56))
      )
    ))

    // connection.insert("INSERT INTO toto1 VALUES", values)


    // val blocks = connection.query("SELECT arr FROM str_arr").toList
    // val blocks = connection.query("SELECT * FROM nested").toList
    // val blocks = connection.query("SELECT * FROM decimal").toList

    val blocks = connection.query("SELECT tuple(65536,'a') as t").toList
    // val blocks = connection.query("SELECT * from uints").toList

    blocks.zipWithIndex.foreach { case (block, i) =>
      println(block.columns)
      // println(s"Block $i " + block.columns(0).asInstanceOf[NullableColumn].nulls.toList)
      block.columns.zipWithIndex.foreach { case (col, j) =>
        println(s"Block $i col $j " + (col match {
          case ArrayColumn(_, data) => data match {
            case Int8Array(arrays) => arrays.map(_.toList).toList
            case ClickhouseGenericArray(arrays) => arrays.map(_.toList).toList
            case DateArray(arrays) => arrays.map(_.toList).toList
            case DateTimeArray(arrays) => arrays.map(_.toList).toList
            case Float32Array(arrays) => arrays.map(_.toList).toList
            case Float64Array(arrays) => arrays.map(_.toList).toList
            case Int32Array(arrays) => arrays.map(_.toList).toList
            case Int64Array(arrays) => arrays.map(_.toList).toList
            case Int16Array(arrays) => arrays.map(_.toList).toList
            case StringArray(arrays) => arrays.map(_.toList).toList
          }
          case DateColumn(_, data) => data.toList
          case DateTimeColumn(_, data) => data.toList
          case EnumColumn(_, _, enums, data) => data.toList.map(enums)
          case FixedStringColumn(_, _, data) => data.toList
          case Float32Column(_, data) => data.toList
          case Float64Column(_, data) => data.toList
          case Int8Column(_, data) => data.toList
          case Int16Column(_, data) => data.toList
          case Int32Column(_, data) => data.toList
          case Int64Column(_, data) => data.toList
          case NullableColumn(_, data, nulls) => data.toList
          case StringColumn(_, data) => data.toList
          case UInt8Column(_, data) => data.toList
          case UInt16Column(_, data) => data.toList
          case UInt32Column(_, data) => data.toList
          case UInt64Column(_, data) => data.toList
          case UuidColumn(_, data) => data.toList
          case TupleColumn(_, data) => data.toList
        }))
      }
    }

    connection.disconnect()
  }
}