package bragi

import java.nio.file.{Files, Paths, StandardOpenOption}

import bragi.model.Command
import com.google.gson.Gson
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener

import scalaj.http.{Http, HttpOptions}

object BragiBotRunner extends LazyLogging{

    val bragiServiceUrl: String = "http://localhost:9090/bragi"

    def main(args: Array[String]) {
        println("What's the Slack API token?")

        val token = scala.io.StdIn.readLine()

        println("Starting the Slack bot runner!")

        val session: SlackSession = SlackSessionFactory.createWebSocketSlackSession(token)
        val listener = new SlackMessagePostedListener {
            override def onEvent(msg: SlackMessagePosted, slackSession: SlackSession): Unit = {
                try {
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

        val args: Array[String] = msg.getMessageContent.split(" ")

        val command = new Command()
        command.action = args(0)
        command.platform = getOption(args, "-p")
        command.term = getOption(args, "-t")

        val gson = new Gson

        val json: String = gson.toJson(command)
        json
    }

    def getOption(args: Array[String], option: String): String = {
        val opt: Int = args.indexOf(option)
        if(opt > 0) {
            return args(opt + 1)
        }

        ""
    }
    def postToServer(json: String) = {
        logger.info("sending post request to {} with json body {}", bragiServiceUrl, json)
        val result = Http(bragiServiceUrl).postData(json)
                .header("Content-Type", "application/json")
                .header("Charset", "UTF-8")
                .option(HttpOptions.readTimeout(10000)).asString
        logger.info("response from bragi service {}", result)
        result
    }


}
