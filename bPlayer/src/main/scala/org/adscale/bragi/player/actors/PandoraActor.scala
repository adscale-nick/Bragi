package org.adscale.bragi.player.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.adscale.bragi.player.actors.AudioPlayer.{Next, Start}
import org.adscale.bragi.player.modules.pandora.PandoraPlayer

case class Action(action: String, station: String)

class PandoraActor(audioActor: ActorRef) extends Actor with ActorLogging {

    def actorRefFactory = context

    implicit def executionContext = actorRefFactory.dispatcher

    override def receive: Receive = {
        case Action("play", station) =>
            log.info(s"Play called with station: $station")
            val player: PandoraPlayer = new PandoraPlayer()
            player.loadStation(if (station == null) "QuickMix" else station)
            audioActor ! Start(player)
        case Action("next", _) =>
            log.info(s"Next called")
            audioActor ! Next
        case Action(action, station) => log.warning(s"Unknown method with action: $action, and station: $station")
        case _ => log.error(s"Shit hit the fan yo")
    }


}
