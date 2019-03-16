package ckh

import ckh.native._
import ckh.protocol._

import java.nio.ByteBuffer

object ProtocolSteps {
  def  sendHello(info: ClientInfo): ProtocolStep = Cont.o { buf =>
    ClientPacketWriters.message.write(info, buf)
    receiveHello
  }

  val receiveHello: ProtocolStep = NeedsInput.i { buf =>
    ServerPacketReaders.protocol.read(buf) match {
      case info: ServerInfo => Emit(info, Done)
      case other => Error("Unexpected packet: " + other)
    }
  }

  val receiveResult: ProtocolStep = multipacket(NeedsInput.i { buf =>
    val packet = ServerPacketReaders.protocol.read(buf)
    packet match {
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
  })

  def execute(q: String, externalTables: Iterator[Block], values: Iterator[Block]): ProtocolStep = Cont.o { buf =>
    ClientPacketWriters.message.write(Query(None, Complete, None, q), buf)
    externalTables.foreach { extBlock =>
      ClientPacketWriters.message.write(ClientDataBlock(extBlock), buf)
    }
    ClientPacketWriters.message.write(ClientDataBlock(Block.empty), buf) // external tables
    receiveSample(sample => sendData(sample, values))
  }

  def receiveSample(nextWithSample: Block => ProtocolStep): ProtocolStep = NeedsInput.i { buf =>
    val packet = ServerPacketReaders.protocol.read(buf)
    packet match {
      case p: ServerDataBlock => Emit(p, Cont(nextWithSample(p.block)))
      case other => Error("Unexpected packet: " + other)
    }
  }

  def sendData(sample: Block, values: Iterator[Block]): ProtocolStep = Cont.o { buf =>
    values.foreach { block =>
      ClientPacketWriters.message.write(ClientDataBlock(block), buf)
    }
    ClientPacketWriters.message.write(ClientDataBlock(Block.empty), buf) // end of data
    receiveResult
  }

  def multipacket(step: ProtocolStep): ProtocolStep = step match {
    case NeedsInput(next) => Cont { (i, o) =>
      if(i.position < i.limit) multipacket(next(i, o))
      else NeedsInput.i(_ => multipacket(NeedsInput(next)))
    }
    case other => other
  }
}