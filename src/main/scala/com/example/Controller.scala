package com.example

import akka.actor.ActorRef
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.example.actor.{LyricsPublisher,LyricsSourceGraphStage}
import com.example.actorSystem.dispatcher
import com.example.domain.Lyrics
import com.example.domain.Lyrics._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps

object Controller {
  private[this] val logger = LoggerFactory getLogger Controller.getClass
  private[this] val start = ByteString("[")
  private[this] val sep = ByteString(",")
  private[this] val end = ByteString("]")
  
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json().withFramingRenderer(Flow[ByteString].intersperse(start, sep, end))
  
  def route: Route = reactiveStream ~ rootPath
  
  private def rootPath = {
    pathSingleSlash {
      get {
        import java.util.concurrent.TimeUnit
        logger.info("Handling the GET request ...")
        val source: Source[Lyrics, ActorRef] = Source.actorPublisher[Lyrics](LyricsPublisher.props)
          .mapMaterializedValue(actorRef => {
            actorSystem.scheduler.scheduleOnce(500 milliseconds) {
              streamLyrics(actorRef)
            }
            actorRef
          })
          .idleTimeout(FiniteDuration(5, TimeUnit.SECONDS))
        complete(source)
      }
    }
  }
  
  private def reactiveStream = {
    path("reactive-stream") {
      complete(Source.fromGraph(new LyricsSourceGraphStage))
    }
  }
  
  def streamLyrics(implicit lyricsPublisher:ActorRef): Unit = {
    logger info("Reading the contents of the classpath file 'bryan-adams-heaven.txt' ...")
    import akka.actor.PoisonPill

    import scala.io.Source
    Source.fromResource("bryan-adams-heaven.txt")
      .getLines
      .foreach(line => {
        // Artificially throttling for demonstration purpose. Otherwise, it'd be too fast to notice what's happening
        Thread.sleep(500L)
        lyricsPublisher ! Lyrics(line)
      })
    lyricsPublisher ! PoisonPill
  }

}
