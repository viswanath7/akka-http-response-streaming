package com.example.application
import com.example._
import akka.http.scaladsl.Http
import com.example.Controller
import org.slf4j.LoggerFactory

object Server {
  private[this] val logger = LoggerFactory getLogger Server.getClass
  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(Controller.route, "localhost", 8080)
    logger info s"Server is now online at http://localhost:8080/"
  }
}
