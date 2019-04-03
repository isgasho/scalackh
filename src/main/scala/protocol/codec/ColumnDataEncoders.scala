package scalackh.protocol.codec

import java.nio.ByteBuffer
import java.time.ZoneOffset

import scalackh.protocol._
import scalackh.protocol.codec.DefaultEncoders._

object ColumnDataEncoders {
  val dateColumnDataEncoder: Encoder[DateColumnData] = Encoder { (col, buf) =>
    col.data.foreach { date =>
      writeShort(date.toEpochDay().toShort, buf)
    }
  }

  val dateTimeColumnDataEncoder: Encoder[DateTimeColumnData] = Encoder { (col, buf) =>
    col.data.foreach { datetime =>
      buf.putInt(datetime.toEpochSecond(ZoneOffset.UTC).toInt)
    }
  }

  // val enum8ColumnDataEncoder: Encoder[Enum8ColumnData] = Encoder { (col, buf) =>
  //   val enumDefs: String = col.enums.toList.sortBy(_._1).map(t => t._1 + " = " + t._2).mkString(",")
  //   writeString(s"Enum8($enumDefs)", buf)
  //   col.data.foreach { e =>
  //     val b: Byte = (e & 0xff).toByte
  //     buf.put(b)
  //   }
  // }

  val fixedStringColumnDataEncoder: Encoder[FixedStringColumnData] = Encoder { (col, buf) =>
    col.data.foreach(str => buf.put(str.getBytes("UTF-8")))
  }

  val float32ColumnDataEncoder: Encoder[Float32ColumnData] = Encoder { (col, buf) =>
    col.data.foreach(writeFloat(_, buf))
  }

  val float64ColumnDataEncoder: Encoder[Float64ColumnData] = Encoder { (col, buf) =>
    col.data.foreach(writeDouble(_, buf))
  }

  val int8ColumnDataEncoder: Encoder[Int8ColumnData] = Encoder { (col, buf) =>
    col.data.foreach(buf.put)
  }

  val int16ColumnDataEncoder: Encoder[Int16ColumnData] = Encoder { (col, buf) =>
    col.data.foreach(writeShort(_, buf))
  }

  val int32ColumnDataEncoder: Encoder[Int32ColumnData] = Encoder { (col, buf) =>
    col.data.foreach(buf.putInt)
  }

  val int64ColumnDataEncoder: Encoder[Int64ColumnData] = Encoder { (col, buf) =>
    col.data.foreach(writeLong(_, buf))
  }

  val stringColumnDataEncoder: Encoder[StringColumnData] = Encoder { (col, buf) =>
    col.data.foreach(writeString(_, buf))
  }

  val uuidColumnDataEncoder: Encoder[UuidColumnData] = Encoder { (col, buf) =>
    col.data.foreach { uuid =>
      buf.putLong(uuid.getMostSignificantBits())
      buf.putLong(uuid.getLeastSignificantBits())
    }
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

  val columnDataEncoder: Encoder[ColumnData] = Encoder { (col, buf) =>
    col match {
      case col: DateColumnData =>
        writeString("Date", buf)
        dateColumnDataEncoder.write(col, buf)
      case col: DateTimeColumnData =>
        writeString("DateTime", buf)
        dateTimeColumnDataEncoder.write(col, buf)
      // case col: Enum8ColumnData => enum8ColumnDataEncoder.write(col, buf)
      case col: FixedStringColumnData =>
        writeString(s"FixedString(${col.strLength})", buf)
        fixedStringColumnDataEncoder.write(col, buf)
      case col: Float32ColumnData =>
        writeString("Float32", buf)
        float32ColumnDataEncoder.write(col, buf)
      case col: Float64ColumnData =>
        writeString("Float64", buf)
        float64ColumnDataEncoder.write(col, buf)
      case col: Int8ColumnData =>
        writeString("Int8", buf)
        int8ColumnDataEncoder.write(col, buf)
      case col: Int16ColumnData =>
        writeString("Int16", buf)
        int16ColumnDataEncoder.write(col, buf)
      case col: Int32ColumnData =>
        writeString("Int32", buf)
        int32ColumnDataEncoder.write(col, buf)
      case col: Int64ColumnData =>
        writeString("Int64", buf)
        int64ColumnDataEncoder.write(col, buf)
      case col: StringColumnData =>
        writeString("String", buf)
        stringColumnDataEncoder.write(col, buf)
      case col: UuidColumnData =>
        writeString("UUID", buf)
        uuidColumnDataEncoder.write(col, buf)
    } 
  }
}