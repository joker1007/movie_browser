package model.dmm

import scala.xml.Node
import scala.collection.mutable.ListBuffer

case class ItemInfo(infoType: String, id: String, name: String)

object ItemInfo {
  def fromXMLNode(node: Node): Option[ItemInfo] = {
    for {
      (id, name) <- parse(node)
    } yield ItemInfo(node.label, id, name)
  }

  def fromXMLNodeToCollection(node: Node): List[ItemInfo] = {
    val buf: ListBuffer[ItemInfo] = ListBuffer()
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
