package org.adscale.bragi.player

import javax.sound.sampled.{AudioInputStream, AudioSystem, Clip}

import org.adscale.bragi.player.modules.pandora.PandoraPlayer

object Player extends App {
    var clip: Clip = null

    var player: AudioService = null

    var currentlyPlaying: Boolean = false

    def play(audioIn: AudioInputStream): Unit = {
        currentlyPlaying = true
        clip.open(audioIn)
        //        println(clip.getFormat)
        //        println(clip.getLineInfo)
        //        val songLengthInMillis: Long = clip.getMicrosecondLength / 1000
        //        val PERIOD_FORMATTER: PeriodFormatter = new PeriodFormatterBuilder().printZeroAlways()
        //                .minimumPrintedDigits(2).appendMinutes.appendSeparator(":").appendSeconds.toFormatter

        println(player.song())
        //        var position: Long = 0L
        clip.start()
        //        while (position < songLengthInMillis) {
        //            position = clip.getMicrosecondPosition / 1000
        //            print(position + "/" + songLengthInMillis + " ")
        //            println(PERIOD_FORMATTER.print(new Period(position)) + "/" + PERIOD_FORMATTER.print(new Period(songLengthInMillis)))
        //            Thread.sleep(1000)
        //        }
    }

    override def main(args: Array[String]) {
        clip = AudioSystem.getClip
        player = new PandoraPlayer()
        player.asInstanceOf[PandoraPlayer].loadStation("galantis")
        play(player.next())
        while (true) {
            if(songEnded){
                clip.close()
                currentlyPlaying = false
                println("Starting new Song...")
                play(player.next())
            }
        }
    }

    def songEnded: Boolean = {
        val songLengthInMillis: Long = clip.getMicrosecondLength / 1000
        val currentPosition = clip.getMicrosecondPosition / 1000
        currentPosition == songLengthInMillis

    }
}