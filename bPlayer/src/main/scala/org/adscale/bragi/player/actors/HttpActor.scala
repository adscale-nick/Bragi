package org.adscale.bragi.player.actors

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.pattern.ask
import org.json4s._
import spray.can.Http
import spray.can.server.Stats
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.duration._
import scala.language.postfixOps

object Json4sProtocol extends Json4sSupport {
    implicit def json4sFormats: Formats = DefaultFormats
}

class HttpActor(playerActor: ActorRef) extends Actor with HttpService with ActorLogging {

    import Json4sProtocol._

//        import PandoraActor._

    val pandoraWorker = actorRefFactory.actorOf(Props(new PandoraActor(playerActor)), "Pandora-Actor")

    def actorRefFactory = context

    implicit def executionContext = actorRefFactory.dispatcher

    implicit val timeout = 5.seconds

    def receive = runRoute(
        path("stats") {
            complete {
                log.info("Stats called")
                actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")
                        .ask(Http.GetStats)(1.second)
                        .mapTo[Stats]
            }
        } ~
                path("pandora") {
                    log.info("Pandora route enabled")
                    get {
                        log.info("Pandora called")
                        parameters('a ? "play", 's?) { (action, station) =>
                            complete {
                                pandoraWorker ! Action(action, station.orNull)
                                "done"
                            }
                        }
                    }
                } ~
                path("youtube") {
                    get {
                        complete("not yet implemented")
                    }
                }
    )

}
