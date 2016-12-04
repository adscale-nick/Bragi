package org.adscale.bragi.player

import javax.sound.sampled.{AudioInputStream, AudioSystem, Clip}

import org.adscale.bragi.player.modules.pandora.PandoraPlayer
import org.slf4j.{LoggerFactory, Logger}


object Player extends App {
    var clip: Clip = null

    var player: AudioService = null

    var currentlyPlaying: Boolean = false

    var log: Logger = null

    override def main(args: Array[String]) {
        log = LoggerFactory.getLogger("org.adscale.bragi.player.Player")
        clip = AudioSystem.getClip
        player = new PandoraPlayer()
        val stationName: String = "Eric Clapton"
        if(player.asInstanceOf[PandoraPlayer].loadStation(stationName)) {
            play(player.next())
            while (true) {
                if(songEnded){
                    clip.close()
                    currentlyPlaying = false
                    log.debug("Starting new Song.")
                    play(player.next())
                }
            }
        }
        else {
            log.error("Could not load given station \'{}\'", stationName)
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