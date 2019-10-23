package com.example.actor

import akka.actor.Props
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Request,Cancel}
import com.example.domain.Lyrics
import org.slf4j.LoggerFactory
import scala.annotation.tailrec

class LyricsPublisher extends ActorPublisher[Lyrics] {
	private[this] val logger = LoggerFactory getLogger getClass.getSimpleName
	private[this] val MaxBufferSize = 256
	private var buffer = Vector.empty[Lyrics]
	@tailrec final def deliverBuffered(): Unit =
		if (totalDemand > 0) {
			// totalDemand is a Long and could be larger than what buffer.splitAt can accept
			if (totalDemand <= Int.MaxValue) {
				val (use, keep) = buffer.splitAt(totalDemand.toInt)
				buffer = keep
				use foreach onNext
			} else {
				val (use, keep) = buffer.splitAt(Int.MaxValue)
				buffer = keep
				use foreach onNext
				deliverBuffered()
			}
		}
	
	override def receive: Receive = {
		case lyrics @ Lyrics(line) =>
			logger info s"Received => $line"
			if (buffer.isEmpty && totalDemand > 0 && isActive) {
				logger.debug("Due to demand from down-stream, sending the incoming elements without buffering it ...")
				onNext(lyrics)
			}
			else {
				logger.debug("Due to lack of demand from down-stream, storing the incoming message in the buffer...")
				buffer :+= lyrics
			}
		case Request(_) =>
			/**
			 * When the stream subscriber requests more elements the ActorPublisherMessage.Request message is delivered to this actor,
			 * so one can act on that event. The totalDemand is updated automatically.
			 */
		logger.debug("Handling request from down stream to send more data ...")
		deliverBuffered()
		case Cancel =>
			logger.debug("Received a cancel message so completing the stream ...")
			onComplete()
			context stop self
			val hasCompleted = if(isCompleted) "Yes" else "No"
			logger.info(s"Has the stream completed? $hasCompleted")
	}
}

object LyricsPublisher {
	def props: Props = Props[LyricsPublisher]
}
