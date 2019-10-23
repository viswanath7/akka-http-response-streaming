package com

import akka.actor.{ActorSystem, ActorPath, Props}
import akka.stream.ActorMaterializer
import com.example.actor.LyricsPublisher

package object example {
	implicit val actorSystem: ActorSystem = ActorSystem("SingerActorSystem")
	implicit val materialiser: ActorMaterializer = ActorMaterializer()
	//implicit val lyricsPublisher = actorSystem.actorOf(Props[LyricsPublisher], "lyrics-publisher")
}
