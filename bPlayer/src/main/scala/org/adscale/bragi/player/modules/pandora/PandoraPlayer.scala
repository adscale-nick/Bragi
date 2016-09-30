package org.adscale.bragi.player.modules.pandora

import java.io.{File, FileOutputStream}
import java.net.URL
import java.nio.channels.{Channels, ReadableByteChannel}
import java.util.{List => JList}
import javax.sound.sampled.AudioInputStream

import be.tarsos.transcoder.{Attributes, DefaultAttributes, Streamer}
import org.adscale.bragi.player.AudioService

import scala.collection.JavaConversions._

class PandoraPlayer extends AudioService {

    var currentStation: Station = null

    var currentSongIndex: Int = 0

    var playlist: Array[Song] = null

    var radio: JsonPandoraRadio = new JsonPandoraRadio()


    val pandoraStorage: String = "/home/nicks/pandora/"

    val attributes: Attributes = DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ.getAttributes
    attributes.setSamplingRate(32000)


    val radioStations: JList[Station] = radio.getStations

    def loadStation(stationString: String): Boolean = {
        for (station: Station <- radioStations) {
            if (station.getName.toLowerCase.contains(stationString)) {
                currentStation = station
                val stationDir: File = new File(pandoraStorage + currentStation.getName)
                if(!stationDir.exists()){
                    stationDir.mkdirs()
                }
                return true
            }
        }
        false
    }


    override def song(): String = {
        playlist(currentSongIndex).getArtist + " - " + playlist(currentSongIndex).getTitle
    }

    override def queue(): Array[String] = ???

    override def next(): AudioInputStream = {
        currentSongIndex = currentSongIndex + 1
        if (playlist == null || (currentSongIndex == playlist.length)) {
            playlist = radio.getPlaylist(currentStation, "JSON")
            currentSongIndex = 0
        }
        val song: Song = playlist(currentSongIndex)
        val songPath: String = pandoraStorage + currentStation.getName + "/" + song.getArtist + " - " + song.getTitle + ".mp3"
        if (!new File(songPath).exists()) {
            val rbc: ReadableByteChannel = Channels.newChannel(new URL(song.getAudioUrl).openStream())
            val fos: FileOutputStream = new FileOutputStream(songPath)
            fos.getChannel.transferFrom(rbc, 0, Long.MaxValue)
        }
        Streamer.stream(songPath, attributes)
    }
}
