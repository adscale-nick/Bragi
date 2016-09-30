package org.adscale.bragi.player.modules.youtube

import java.io.File
import java.net.URL
import java.util.{List => JList}
import javax.sound.sampled._

import be.tarsos.transcoder.{Attributes, DefaultAttributes, Streamer}
import com.github.axet.vget.VGet


object YoutubePlayer {
    def getSong(searchTerm: String): AudioInputStream = {

        val resource: URL = this.getClass.getResource("/" + searchTerm)
        val attributes: Attributes = DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ.getAttributes
        attributes.setSamplingRate(16789)
        Streamer.stream(search("theThing"), attributes)

    }

    def search(searchTerm: String): String = {
        try {
            // ex: "/Users/axet/Downloads"
            val path: File = new File("/tmp/youtubeCache/")
            if (!path.exists()) {
                path.mkdir()
            }
            val v: VGet = new VGet(new URL("https://www.youtube.com/watch?v=TGnKOkPIJR0"), path)
            v.download()
            path.getAbsolutePath
        } catch {
            case e: Exception =>
                throw new RuntimeException(e)
        }

        //http://www.youtube.com/get_video_info?video_id=TGnKOkPIJR
    }

}

