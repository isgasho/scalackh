package scalackh.protocol.steps

import scalackh.protocol._
import scalackh.protocol.codec._

object ProtocolSteps {
  val MAX_ROWS_IN_BLOCK = 10000

  def  sendHello(info: ClientInfo): ProtocolStep = Cont.o { buf =>
    ClientPacketEncoders.message.write(info, buf)
    receiveHello
  }

  val receiveHello: ProtocolStep = NeedsInput.i { buf =>
    val decodedPacket: DecoderResult[ServerPacket] = ServerPacketDecoders.protocol.read(buf)

    decodedPacket match {
      case Consumed(packet) => packet match {
        case info: ServerInfo => Emit(info, Done)
        case other => Error("Unexpected packet: " + other)
      }
      case NotEnough => receiveHello
    }
  }

  val receiveResult: ProtocolStep = Cont.i { buf =>
    val decodedPacket: DecoderResult[ServerPacket] = ServerPacketDecoders.protocol.read(buf)
    decodedPacket match {
      case NotEnough => NeedsInput(receiveResult)
      case Consumed(packet) => packet match {
        case p: ServerInfo => Error("Unexpected packet: " + p)
        case p: ServerDataBlock => Emit(p, receiveResult)
        case p: Progress => Emit(p, receiveResult)
        case p: ProfileInfo => Emit(p, receiveResult)
        case p: TotalsBlock => Emit(p, receiveResult)
        case p: ExtremesBlock => Emit(p, receiveResult)
        case p: LogBlock => Emit(p, receiveResult)
        case p: ServerException => Emit(p, Done)
        case Pong => Emit(Pong, receiveResult)
        case EndOfStream => Done
      }
    }
  }

  def execute(q: String, externalTables: Iterator[Block], values: Iterator[Block], settings: Map[String, Any]): ProtocolStep = Cont.o { buf =>
    ClientPacketEncoders.message.write(Query(None, settings, Complete, None, q), buf)
    externalTables.foreach { extBlock =>
      ClientPacketEncoders.message.write(ClientDataBlock(extBlock), buf)
    }
    ClientPacketEncoders.message.write(ClientDataBlock(Block.empty), buf) // external tables
    val nextWithSample = {
      if(values.isEmpty) (_: Block) => receiveResult
      else (sample: Block) => sendData(sample, values)(receiveResult)
    }
    receiveSample(nextWithSample)
  }

  def receiveSample(nextWithSample: Block => ProtocolStep): ProtocolStep = NeedsInput.i { buf =>
    val decoderPacket: DecoderResult[ServerPacket] = ServerPacketDecoders.protocol.read(buf)
    decoderPacket match {
      case NotEnough => receiveSample(nextWithSample)
      case Consumed(packet) => packet match {
        case p: ServerDataBlock => Emit(p, Cont(nextWithSample(p.block)))
        case other => Error("Unexpected packet: " + other)
      }
    }
  }

  def sendData(sample: Block, values: Iterator[Block])(afterSend: => ProtocolStep): ProtocolStep = Cont.o { buf =>
    if(values.hasNext) {
      val block = values.next()
      val blockWithNames = sample.copy(
        nbRows = block.nbRows,
        nbColumns = block.nbColumns,
        columns = sample.columns.zip(block.columns).map { case (sampleCol, dataCol) =>
          dataCol.copy(name = sampleCol.name)
        }
      )
      sendBlock(blockWithNames)(sendData(sample, values)(afterSend))
    }
    else {
      ClientPacketEncoders.message.write(ClientDataBlock(Block.empty), buf) // end of data
      afterSend
    }
  }

  def sendBlock(block: Block)(next: => ProtocolStep): ProtocolStep = Cont.o { buf =>
    if(block.nbRows <= MAX_ROWS_IN_BLOCK) {
      ClientPacketEncoders.message.write(ClientDataBlock(block), buf)
      next
    }
    else { // block too big, split it
      val (maxSizeBlock, remainingBlock) = Split.splitBlock(MAX_ROWS_IN_BLOCK)(block)
      ClientPacketEncoders.message.write(ClientDataBlock(maxSizeBlock), buf)
      sendBlock(remainingBlock)(next)
    }
  }
}