package templates

import org.scalatra.Route
import org.scalatra.UrlGenerator.url
import scala.collection.JavaConversions._

object PaginationHelper {
  def paginate[A](baseUrl: Route, collection: Seq[A], currentPage: Int, totalCount: Int, perPage: Int)(implicit req: javax.servlet.http.HttpServletRequest) = {
    val lastPage = (totalCount / perPage) + 1
    val params = req.getParameterMap
    val pairs = Map(params.keys.map {k => (k, params.get(k).head)}.toSeq: _*)
    <ul class="pagination">
      <li><a href={url(baseUrl, (pairs + ("page" -> "1")).toSeq: _*)}>&laquo;</a></li>
      {for (i <- 1 to lastPage) yield <li class={if (i == currentPage) "active" else ""}><a href={url(baseUrl, (pairs + ("page" -> i.toString)).toSeq: _*)}>{i}</a></li> }
      <li><a href={url(baseUrl, (pairs + ("page" -> lastPage.toString)).toSeq: _*)}>&raquo;</a></li>
    </ul>
  }
}
