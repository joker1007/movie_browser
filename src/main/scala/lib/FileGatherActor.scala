package lib

import akka.actor.{Props, Actor}
import model.{Fileinfo, Target}
import org.joda.time.DateTime
import scalax.file.Path
import akka.routing.RoundRobinRouter

class FileGatherActor extends Actor {
  def receive = {
    case Target(id, fullpath, _, _, _) =>
      val router = context.actorOf(Props[FileInfoCreateWorker].withRouter(RoundRobinRouter(nrOfInstances = 4)))
      FileGather.gather(fullpath, router)
      Target.updateById(id).withAttributes('last_updated_at -> Some(DateTime.now()))
  }
}

class FileInfoCreateWorker extends Actor {
  def receive = {
    case file: Path =>
      val oid = Fileinfo.createFromFile(file)
      oid match {
        case Some(id) => println(s"Create: ${file.toString()}")
        case None =>
      }
  }
}