package com.example.actor

import com.example.domain.Lyrics
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import org.slf4j.LoggerFactory

/**
 * Source, Flow and Sink are specialised graph stages whose type is determined by the number of inputs and outputs.
 * A Source is a specialised graph stage that has no input and a single output.
 * A Sink has a single input and no outputs
 * A Flow has a single input and a single output.
 * Each part of the graph is a GraphStage with a given Shape; the most basic shapes being SourceShape, FlowShape and SinkShape.
 * In our case, the LyricsSourceGraphStage has no input but a single output
 */
class LyricsSourceGraphStage extends GraphStage[SourceShape[Lyrics]] {
	
	private[this] val logger = LoggerFactory getLogger getClass.getSimpleName
	
	// To define a port, one needs to provide its name and data type
	val outlet: Outlet[Lyrics] = Outlet("LyricsSource")
	override def shape: SourceShape[Lyrics] = SourceShape(outlet)
	
	override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
		import scala.io.Source
		/*
			All state MUST be inside the GraphStageLogic, never inside the enclosing GraphStage
			A GraphStage is meant to be reusable and hence it must remain immutable.
			On the other hand, a new instance of GraphStageLogic is created for every materialization.
			It is the one and only place to keep the mutable state in.
			This state is safe to access and modify from all the callbacks that are provided by GraphStageLogic and registered handlers.
		 */
		private var lines = Source.fromResource("twenty-one-pilots-stressed-out.txt")
			.getLines
  		.map(Lyrics(_))
			.toList
		/*
			Apart from keeping the mutable state, one may also define handlers for the onPush() and onPull() events.
			The onPush() event occurs when a new element from the upstream is available and can be acquired using grab()
			The onPull(), on the other hand, occurs when the downstream is ready to accept a new element which can be sent with push()
		 */
		setHandler(outlet, new OutHandler {
			override def onPull(): Unit = {
				lines match {
					case Nil =>
						logger debug s"Completing the stream ..."
						complete(outlet)
					case head :: tail =>
						logger debug s"Pushing down stream >>> ${head.line}"
						// Artificially throttling for demonstration purpose.
						Thread.sleep(100L)
						emit(outlet, head)
						lines = tail
				}
			}
		})
	}
}
