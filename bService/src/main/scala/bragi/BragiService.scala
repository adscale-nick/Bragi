package bragi

import java.nio.file.{Files, Paths, StandardOpenOption}

import akka.actor._
import bragi.handler.SearchHandler
import bragi.model.Filter
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
trait BragiService extends HttpService {

    implicit def executionContext = actorRefFactory.dispatcher

    val myRoute = {
        get {
            pathSingleSlash {
                complete(index)
            }
            path("search") {
                complete(<html><body><p>what am I searching for?</p></body></html>)
            }
        } ~
        (post | parameter('method ! "post")) {
            path("search") {
                entity(as[Filter]) { filter => {
                    try {
                        var message = "\n==============================\n"
                        message += "search " + filter.platform + " for " + filter.term
                        message += "\n==============================\n"
                        Files.write(Paths.get("/tmp/bragi-server/bragi.txt"), message.getBytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                    } catch {
                        case e: Exception => println("Caught exception")
                    }
                    complete(filter.toString)
                }

                }
            }
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