package org.adscale.bragi.player.actors

import javax.sound.sampled.{AudioInputStream, AudioSystem, Clip}

import akka.actor.{Actor, ActorLogging}
import org.adscale.bragi.player.AudioService

object AudioPlayer {

    case class Start(service: AudioService)

    case class Stop()

    case class Pause()

    case class Next()

}

class AudioPlayer extends Actor with ActorLogging {
    def actorRefFactory = context

    import org.adscale.bragi.player.actors.AudioPlayer._

    implicit def executionContext = actorRefFactory.dispatcher

    val thread: Thread = new Thread(new RunPlayer)
    var currentlyPlaying: Boolean = false
    var clip: Clip = null

    var player: AudioService = null

    override def receive: Receive = {
        case Start(service) =>
            log.info("Start called on player")
            this.player = service
            if (!currentlyPlaying && !thread.isAlive) {
                log.info("Initialising Player.")
                clip = AudioSystem.getClip
                thread.start()
            }
            else {
                clip.close()
                currentlyPlaying = false
                play(player.next())
            }
        case Next =>
            log.info("Next called on player")
            clip.close()
            currentlyPlaying = false
            play(player.next())
    }


    class RunPlayer extends Runnable {
        def run(): Unit = {
            log.info("Play thread started, grabbing next song")
            play(player.next())
            log.info("Starting Loop")
            while (true) {
                if (songEnded) {
                    clip.close()
                    currentlyPlaying = false
                    log.debug("Song ended, auto-starting new one.")
                    play(player.next())
                }
            }
        }
    }

    def play(audioIn: AudioInputStream): Unit = {
        if (!currentlyPlaying) {
            currentlyPlaying = true
            clip.open(audioIn)
            log.info(player.song())
        }
        clip.start()
    }

    def songEnded: Boolean = {
        val songLengthInMillis: Long = clip.getMicrosecondLength / 1000
        val currentPosition = clip.getMicrosecondPosition / 1000
        currentPosition == songLengthInMillis
    }

    //    def actorRefFactory = context
    //
    //    def receive = runRoute(defaultRoute)
    //
    //    var clip: Clip = null
    //
    //    var player: AudioService = null
    //
    //    var currentlyPlaying: Boolean = false
    //
    //    var log: Logger = LoggerFactory.getLogger(classOf[PandoraActor])
    //
    //    val thread: Thread = new Thread(new RunPlayer)
    //
    //    val defaultRoute = path("pandora") {
    //        get {
    //            parameters('action ? "play", 'station ? "QuickMix") { (action, station) =>
    //                var route: StandardRoute = complete("")
    //                action match {
    //                    case "start" =>
    //                        if (!currentlyPlaying) {
    //                            clip = AudioSystem.getClip
    //                            player = new PandoraPlayer()
    //                            if (player.asInstanceOf[PandoraPlayer].loadStation(station)) {
    //                                thread.start()
    //                            }
    //                            else {
    //                                log.error("Could not load given station \'{}\'", station)
    //                            }
    //                        }
    //                        else{
    //                            player.asInstanceOf[PandoraPlayer].loadStation(station)
    //                            clip.close()
    //                            currentlyPlaying = false
    //                            play(player.next())
    //                        }
    //                    case "next" =>
    //                        clip.close()
    //                        currentlyPlaying = false
    //                        play(player.next())
    //                    case "queue" =>
    //                        route = complete(player.queue())
    //                    case "song" =>
    //                        route = complete(player.song())
    //                    case "pause" =>
    //                        pause()
    //                    case "stop" =>
    //                        stop()
    //                    case "play" =>
    //                        if (currentlyPlaying) {
    //                            play(null)
    //                        }
    //                        else{
    //                            play(player.next())
    //                        }
    //                    case _ =>
    //                        route = complete("Not yet implemented")
    //                }
    //                route
    //            }
    //        }
    //    }
    //

    //    def play(audioIn: AudioInputStream): Unit = {
    //        if(!currentlyPlaying) {
    //            currentlyPlaying = true
    //            clip.open(audioIn)
    //            log.info(player.song())
    //        }
    //        clip.start()
    //    }
    //
    //    def pause(): Unit ={
    //        clip.stop()
    //    }
    //
    //    def stop(): Unit ={
    //        clip.close()
    //        currentlyPlaying = false
    //    }
    //
    //    def songEnded: Boolean = {
    //        val songLengthInMillis: Long = clip.getMicrosecondLength / 1000
    //        val currentPosition = clip.getMicrosecondPosition / 1000
    //        currentPosition == songLengthInMillis
    //    }
}
