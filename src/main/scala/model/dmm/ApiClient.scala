package model.dmm

import org.joda.time.DateTime
import scalax.io.Resource
import scalax.io.Codec
import java.net.URLEncoder
import scala.xml.XML

object ApiClient {
  private val API_ID = "6UeMhSWbysEDKYX7PwRP"
  private val AFFILIATE_ID = "j4d1007-990"
  private val ENDPOINT = s"http://affiliate-api.dmm.com/"

  private[this] def requestUrl(query: Map[String, String]): String = {
    val base = Map("api_id" -> API_ID, "affiliate_id" -> AFFILIATE_ID, "operation" -> "ItemList", "version" -> "2.00", "timestamp" -> currentTimestamp)
    val q = (base ++ query) map (pair => s"${pair._1}=${URLEncoder.encode(pair._2, "EUC_JP")}") mkString "&"
    ENDPOINT + "?" + q
  }

  private[this] def currentTimestamp: String = {
    DateTime.now().toString("yyyy-MM-dd HH:mm:ss")
  }

  def fetch(query: Map[String, String]): List[DmmItem] = {
    val res = Resource.fromURL(requestUrl(query)).string(Codec("EUC_JP"))
    val nodes = XML.loadString(res)
    val items = nodes \\ "item" map {node =>
      val itemInfoNode = (node \\ "iteminfo").head
      val itemInfos = DmmItemInfo.fromXMLNodeToCollection(itemInfoNode)
      val contentId = (node \\ "content_id").head
      val title = (node \\ "title").head
      val url = (node \\ "URL").head
      val imageUrl = (node \\ "imageURL" \\ "list").headOption
      val largeImageUrl = (node \\ "imageURL" \\ "large").headOption
      val sampleImageUrls = (node \\ "sampleImageURL" \\ "sample_s" \\ "image").toList
      DmmItem(contentId.text, title.text, url.text, imageUrl.map(_.text), largeImageUrl.map(_.text), itemInfos, sampleImageUrls.map(_.text))
    }
    items.toList
  }
}
