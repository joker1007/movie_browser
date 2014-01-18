package lib

import scalax.file.Path
import scalax.file.PathMatcher.{IsFile, IsDirectory}
import model.{Target, Fileinfo}
import akka.actor.ActorRef

object FileGather {
  def gather(target: Target, router: ActorRef) {
    val path = Path(target.fullpath, '/')
    if (!path.isDirectory)
      return

    def processDir(path: Path, router: ActorRef) {
      val pathSet = path.children()
      pathSet.foreach {
        case IsDirectory(f) =>
          processDir(f, router)
        case IsFile(f) =>
          router ! (target, f)
      }
    }

    processDir(path, router)
  }
}
