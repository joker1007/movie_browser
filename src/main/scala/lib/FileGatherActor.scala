package lib

import akka.actor.Actor
import model.Target
import org.joda.time.DateTime

class FileGatherActor extends Actor {
  def receive = {
    case Target(id, fullpath, _, _, _) =>
      FileGather.gather(fullpath)
      Target.updateById(id).withAttributes('last_updated_at -> Some(DateTime.now()))
  }
}
