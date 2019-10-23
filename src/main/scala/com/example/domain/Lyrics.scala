package com.example.domain

import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

case class Lyrics(line:String)

object Lyrics {
	implicit val lyricsJsonFormat: RootJsonFormat[Lyrics] = jsonFormat1(Lyrics.apply)
	implicit def lyricsMarshaller: ToEntityMarshaller[Lyrics] = Marshaller.oneOf(
		Marshaller.withFixedContentType(ContentTypes.`application/json`) { lyrics =>
			HttpEntity(ContentTypes.`application/json`, lyrics.toJson.compactPrint)
		})
}
