package lib

import scalax.file.Path
import scalax.file.PathMatcher.{IsFile, IsDirectory}
import model.Fileinfo
import akka.actor.ActorRef

object FileGather {
  def gather(pathName: String, router: ActorRef) {
    val path = Path(pathName, '/')
    gather(path, router)
  }

  def gather(path: Path, router: ActorRef) {
    val pathSet = path.children()
    pathSet.foreach {
      case IsDirectory(f) =>
        gather(f, router)
      case IsFile(f) =>
        process(f, router: ActorRef)
    }
  }

  def process(file: Path, router: ActorRef) {
    router ! file
  }
}
