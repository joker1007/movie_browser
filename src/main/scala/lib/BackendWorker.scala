package lib

import akka.actor.{Props, Actor}
import model.{Fileinfo, Target}
import org.joda.time.DateTime
import scalax.file.Path
import akka.routing.RoundRobinRouter

trait BatchCommand

case object GatherCommand extends BatchCommand
case object FetchMetadataCommand extends BatchCommand

class BackendWorker extends Actor {
  var gatherWorker = context.actorOf(Props[FileGatherWorker])
  var fetchMetadataWorker = context.actorOf(Props[FetchMetadataWorker])

  def receive = {
    case (GatherCommand, t: Target) =>
      gatherWorker ! t
    case (FetchMetadataCommand, fi: Fileinfo) =>
      fetchMetadataWorker ! fi
  }
}

class FileGatherWorker extends Actor {
  def receive = {
    case Target(id, fullpath, _, _, _) =>
      val router = context.actorOf(Props[FileInfoCreateWorker].withRouter(RoundRobinRouter(nrOfInstances = 4)))
      FileGather.gather(fullpath, router)
      Target.updateById(id).withAttributes('last_updated_at -> Some(DateTime.now()))
  }
}

class FetchMetadataWorker extends Actor {
  def receive = {
    case fi: Fileinfo =>
      fi.createMetadataFromDMM()
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