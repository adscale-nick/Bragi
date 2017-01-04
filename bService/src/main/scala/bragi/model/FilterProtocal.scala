package bragi.model

import spray.httpx._
import spray.json._


case class Filter(action: String, platform: String, term: String)

object FilterProtocol extends  DefaultJsonProtocol with SprayJsonSupport{
    implicit val filter = jsonFormat3(Filter)
}
