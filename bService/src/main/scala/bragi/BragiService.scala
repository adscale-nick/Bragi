package bragi

import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.actor._
import bragi.handler.SearchHandler
import bragi.model.Filter
import com.typesafe.scalalogging.LazyLogging
import org.adscale.bragi.player.actors.{Action, AudioPlayer, PandoraActor}
import spray.routing.HttpService
// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class BragiServiceActor extends Actor with BragiService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

import bragi.model.FilterProtocol._

// this trait defines our service behavior independently from the service actor
trait BragiService extends HttpService with LazyLogging {

    implicit def executionContext = actorRefFactory.dispatcher

    val playerActor = actorRefFactory.actorOf(Props[AudioPlayer], "Http-Service");

    val pandoraWorker = actorRefFactory.actorOf(Props(new PandoraActor(playerActor)), "Pandora-Actor")

    val myRoute = {
        get {
            pathSingleSlash {
                complete(index)
            }
            path("bragi") {
                complete(<html><body><p>what am I searching for?</p></body></html>)
            }
        } ~
        (post | parameter('method ! "post")) {
            path("bragi") {
                entity(as[Filter]) { filter => {
                    try {
                        logger.info("received a post request to bragi body: {}", filter)
                        complete {
                            processRequest(filter)
                            "done"
                        }
                    } catch {
                        case e: Exception => {
                            logger.error("Game over. {}", e)
                            throw new RuntimeException(e)
                        }
                    }
                }

                }
            }
        }
    }

    def processRequest(filter: Filter): Unit = {
        filter match {
            case Filter("play", "pandora", _) => pandoraWorker ! Action(filter.action, filter.term)
            case _ => logger.error("service not implemented request {}", filter)

        }

    }

    lazy val index =
        <html>
            <body>
                <h1>Bragi is alive!</h1>
                <p>Defined resources:</p>
                <ul>
                    <li><a href="/search">/search</a></li>
                </ul>
            </body>
        </html>
}