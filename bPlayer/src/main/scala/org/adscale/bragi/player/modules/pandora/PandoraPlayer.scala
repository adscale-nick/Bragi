package org.adscale.bragi.player.modules.pandora

import java.io.{File, FileOutputStream, IOException}
import java.net.URL
import java.nio.channels.{Channels, ReadableByteChannel}
import java.util.{List => JList}
import javax.sound.sampled.AudioInputStream

import be.tarsos.transcoder.{Attributes, DefaultAttributes, Streamer}
import com.google.common.base.Joiner
import com.typesafe.config.{Config, ConfigFactory}
import org.adscale.bragi.player.AudioService
import org.slf4j.LoggerFactory
import org.slf4j.Logger

import scala.collection.JavaConversions._

class PandoraPlayer extends AudioService {

    val log: Logger = LoggerFactory.getLogger(classOf[PandoraPlayer])

    var currentStation: Station = null

    var currentSongIndex: Int = 0

    var playlist: Array[Song] = null

    val config: Config = ConfigFactory.load()

    val pandoraStorage: String = config.getString("local.pandoraStorage")

    val attributes: Attributes = DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ.getAttributes
    attributes.setSamplingRate(32000)

    var radio: JsonPandoraRadio = new JsonPandoraRadio()

    radio.connect("nic4eva@gmail.com", "gundam1")
    val radioStations: JList[Station] = radio.getStations
    log.debug("Available stations: \n\t => {}", Joiner.on("\n\t => ").join(radioStations))

    def loadStation(stationString: String): Boolean = {
        for (station: Station <- radioStations) {
            if (station.getName.toLowerCase.contains(stationString.toLowerCase)) {
                log.info("Loading station: {}", station.getName)
                currentStation = station
                val stationDir: File = new File(pandoraStorage + currentStation.getName)
                if (!stationDir.exists()) {
                    log.info("Made new directory for station: {}", station.getName)
                    stationDir.mkdirs()
                }
                playlist = radio.getPlaylist(currentStation, "JSON")
                log.info("Station loaded successfully.")
                return true
            }
        }
        false
    }


    override def song(): String = {
        val artist = playlist(currentSongIndex).getArtist
        val title: String = playlist(currentSongIndex).getTitle
        s"[$currentStation] $artist - $title"
    }

    override def queue(): String = {
        var queueString: String = ""
        for (song <- playlist.slice(currentSongIndex+1, playlist.length)) {
            queueString = queueString + song.getArtist + " - " + song.getTitle + "\n"
        }
        queueString
    }

    override def next(): AudioInputStream = {
        log.info("Next Song Called")
        currentSongIndex = currentSongIndex + 1
        if (playlist == null || (currentSongIndex == playlist.length)) {
            playlist = radio.getPlaylist(currentStation, "JSON")
            currentSongIndex = 0
        }
        val song: Song = playlist(currentSongIndex)
        val songPath: String = pandoraStorage + currentStation.getName + "/" + song.getArtist + " - " + song.getTitle + ".mp3"
        log.info("Next song: {}", songPath)
        try {
            if (!new File(songPath).exists()) {
                log.info("Downloading file")
                val rbc: ReadableByteChannel = Channels.newChannel(new URL(song.getAudioUrl).openStream())
                val fos: FileOutputStream = new FileOutputStream(songPath)
                fos.getChannel.transferFrom(rbc, 0, Long.MaxValue)
                log.info("File downloaded, opening...")
            }
            log.info("Starting song")
            Streamer.stream(songPath, attributes)
        }
        catch {
            case e: IOException =>
                log.error("Problem saving file: {}", songPath)

            next()
        }
    }
}
