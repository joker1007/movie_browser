package model.dmm

import scala.xml.Node
import scala.collection.mutable.ListBuffer

case class DmmItemInfo(infoType: String, id: String, name: String)

object DmmItemInfo {
  def fromXMLNode(node: Node): Option[DmmItemInfo] = {
    for {
      (id, name) <- parse(node)
    } yield DmmItemInfo(node.label, id, name)
  }

  def fromXMLNodeToCollection(node: Node): List[DmmItemInfo] = {
    val buf: ListBuffer[DmmItemInfo] = ListBuffer()
    node.child foreach {c =>
      fromXMLNode(c) match {
        case Some(itemInfo) => buf += itemInfo
        case None =>
      }
    }
    buf.toList
  }

  private[this] def parse(node: Node): Option[(String, String)] = {
    for {
      name <- (node \ "name").headOption
      id <- (node \ "id").headOption
    } yield (id.text, name.text)
  }
}
