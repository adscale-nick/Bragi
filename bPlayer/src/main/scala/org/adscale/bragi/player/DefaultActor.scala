package org.adscale.bragi.player

import javax.sound.sampled.{AudioInputStream, AudioSystem, Clip}

import akka.actor.Actor
import org.adscale.bragi.player.modules.pandora.PandoraPlayer
import org.slf4j.{Logger, LoggerFactory}
import spray.routing.{HttpService, StandardRoute}

class DefaultActor extends Actor with HttpService {
    def actorRefFactory = context

    def receive = runRoute(defaultRoute)

    var clip: Clip = null

    var player: AudioService = null

    var currentlyPlaying: Boolean = false

    var log: Logger = LoggerFactory.getLogger(classOf[DefaultActor])

    val thread: Thread = new Thread(new RunPlayer)

    val defaultRoute = path("player") {
        get {
            parameters('action ? "play", 'station ? "QuickMix") { (action, station) =>
                var route: StandardRoute = complete("")
                action match {
                    case "start" =>
                        if (!currentlyPlaying) {
                            clip = AudioSystem.getClip
                            player = new PandoraPlayer()
                            if (player.asInstanceOf[PandoraPlayer].loadStation(station)) {
                                thread.start()
                            }
                            else {
                                log.error("Could not load given station \'{}\'", station)
                            }
                        }
                        else{
                            player.asInstanceOf[PandoraPlayer].loadStation(station)
                            clip.close()
                            currentlyPlaying = false
                            play(player.next())
                        }
                    case "next" =>
                        clip.close()
                        currentlyPlaying = false
                        play(player.next())
                    case "queue" =>
                        route = complete(player.queue())
                    case "song" =>
                        route = complete(player.song())
                    case "pause" =>
                        pause()
                    case "play" =>
                        if (currentlyPlaying) {
                            play(null)
                        }
                        else{
                            play(player.next())
                        }
                    case _ =>
                        route = complete("Not yet implemented")
                }
                route
            }
        }
    }

    class RunPlayer extends Runnable {
        def run(): Unit = {
            play(player.next())
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
        if(!currentlyPlaying) {
            currentlyPlaying = true
            clip.open(audioIn)
            log.info(player.song())
        }
        clip.start()
    }

    def pause(): Unit ={
        clip.stop()
    }

    def songEnded: Boolean = {
        val songLengthInMillis: Long = clip.getMicrosecondLength / 1000
        val currentPosition = clip.getMicrosecondPosition / 1000
        currentPosition == songLengthInMillis
    }
}
