package bragi

import java.nio.file.{Files, Paths, StandardOpenOption}

import bragi.model.Instruction
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener

import scalaj.http.{Http, HttpOptions}

object BotRunner {

    def main(args: Array[String]) {
        println("What's the Slack API token?")

        val token = scala.io.StdIn.readLine()

        println("Starting the Slack bot runner!")

        val session: SlackSession = SlackSessionFactory.createWebSocketSlackSession(token)
        val listener = new SlackMessagePostedListener {
            override def onEvent(msg: SlackMessagePosted, slackSession: SlackSession): Unit = {
                try {
                    var message = "\n==============================\n"
                    message += generateJson(msg)
                    message += "\n==============================\n"
                    Files.write(Paths.get("/tmp/bragi.txt"), message.getBytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                    postToServer(generateJson(msg))
                } catch {
                    case e: Exception => println("Caught exception")
                }
            }
        }
        session.addMessagePostedListener(listener)
        session.connect()

        println("Slackbot started!")
    }


    def generateJson(msg: SlackMessagePosted): String = {

        val commands: Array[String] = msg.getMessageContent.split(":")

        var json = ""

        if(commands.length == 2) {
            json = "{\"platform\": \"" + commands(0) + "\", \"term\": \"" + commands(1) + "\"}"
        }
        else {
            println("invalid command")
        }

        json
    }

    def postToServer(json: String) = {
        val result = Http("http://localhost:9090/search").postData(json)
                .header("Content-Type", "application/json")
                .header("Charset", "UTF-8")
                .option(HttpOptions.readTimeout(10000)).asString
        result
    }


}
