package scalackh.client.derivation

import scalackh.protocol._
import scalackh.transpose.ColumnTransposer

object DerivationUtils {
  def blockIterator[A](as: Iterator[A], groupSize: Int)(implicit colA: ColumnTransposer[A]): Iterator[Block] = as.grouped(groupSize).map { group =>
    val valuesForBlock = group.toList
    val columns: List[ColumnData] = colA.toColumnsData(valuesForBlock)
    Block(None, BlockInfo.empty, columns.length, valuesForBlock.length, columns.map(Column("", _)))
  }
}