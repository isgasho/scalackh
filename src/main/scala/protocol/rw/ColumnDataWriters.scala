package scalackh.protocol.rw

import java.nio.ByteBuffer
import java.time.ZoneOffset

import scalackh.protocol._
import scalackh.protocol.rw.DefaultWriters._

object ColumnDataWriters {
  val dateColumnDataWriter: Writer[DateColumnData] = Writer { (col, buf) =>
    writeString("Date", buf)

    col.data.foreach { date =>
      writeShort(date.toEpochDay().toShort, buf)
    }
  }

  val datetimeColumnDataWriter: Writer[DateTimeColumnData] = Writer { (col, buf) =>
    writeString("DateTime", buf)

    col.data.foreach { datetime =>
      writeInt(datetime.toEpochSecond(ZoneOffset.UTC).toInt, buf)
    }
  }

  // val enum8ColumnDataWriter: Writer[Enum8ColumnData] = Writer { (col, buf) =>
  //   val enumDefs: String = col.enums.toList.sortBy(_._1).map(t => t._1 + " = " + t._2).mkString(",")
  //   writeString(s"Enum8($enumDefs)", buf)
  //   col.data.foreach { e =>
  //     val b: Byte = (e & 0xff).toByte
  //     buf.put(b)
  //   }
  // }

  val float32ColumnDataWriter: Writer[Float32ColumnData] = Writer { (col, buf) =>
    writeString("Float32", buf)
    col.data.foreach(writeFloat(_, buf))
  }

  val float64ColumnDataWriter: Writer[Float64ColumnData] = Writer { (col, buf) =>
    writeString("Float64", buf)
    col.data.foreach(writeDouble(_, buf))
  }

  val int8ColumnDataWriter: Writer[Int8ColumnData] = Writer { (col, buf) =>
    writeString("Int8", buf)
    col.data.foreach(buf.put)
  }

  val int16ColumnDataWriter: Writer[Int16ColumnData] = Writer { (col, buf) =>
    writeString("Int16", buf)
    col.data.foreach(writeShort(_, buf))
  }

  val int32ColumnDataWriter: Writer[Int32ColumnData] = Writer { (col, buf) =>
    writeString("Int32", buf)
    col.data.foreach(writeInt(_, buf))
  }

  val int64ColumnDataWriter: Writer[Int64ColumnData] = Writer { (col, buf) =>
    writeString("Int64", buf)
    col.data.foreach(writeLong(_, buf))
  }

  val stringColumnDataWriter: Writer[StringColumnData] = Writer { (col, buf) =>
    writeString("String", buf)
    col.data.foreach(writeString(_, buf))
  }

  def writeShort(s: Short, buf: ByteBuffer): Unit = {
    buf.putShort(s)
    ()
  }

  def writeLong(n: Long, buf: ByteBuffer): Unit = {
    buf.putLong(n)
    ()
  }

  def writeDouble(n: Double, buf: ByteBuffer): Unit = {
    buf.putDouble(n)
    ()
  }

  def writeFloat(n: Float, buf: ByteBuffer): Unit = {
    buf.putFloat(n)
    ()
  }
}