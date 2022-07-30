package com.ruchij.web.routes

import cats.effect.kernel.Async
import cats.implicits._
import com.ruchij.types.FunctionKTypes._
import com.ruchij.types.Logger
import com.ruchij.web.responses.ResultResponse
import fs2.Stream
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Close
import org.http4s.{HttpRoutes, Request, Response}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object LoggingRoutes {
  private val logger = Logger[LoggingRoutes.type]

  def apply[F[_]: Async](webSocketBuilder2: WebSocketBuilder2[F])(implicit http4sDsl: Http4sDsl[F]): HttpRoutes[F] = {
    import http4sDsl._

    val loggers: Map[String, String => F[Unit]] =
      Map(
        "info" -> logger.info[F],
        "error" -> (message => logger.error[F](message, new Exception("This is an exception"))),
        "warn" -> logger.warn[F],
        "debug" -> logger.debug[F],
        "trace" -> logger.trace[F]
      )

    HttpRoutes.of {
      loggers
        .foldLeft[PartialFunction[Request[F], F[Response[F]]]](PartialFunction.empty) {
          case (routes, (logType, log)) =>
            routes.orElse {
              case (GET | POST) -> Root / `logType` =>
                log(s"Logged $logType message").productR {
                  Ok {
                    ResultResponse(s"${logType.toUpperCase} message has been logged")
                  }
                }
            }
        }
        .orElse {
          case GET -> Root / "ws" =>
            webSocketBuilder2.build(
              loggers
                .foldLeft[Stream[F, WebSocketFrame]](Stream.empty) {
                  case (stream, (logType, log)) =>
                    stream ++
                      Stream.eval {
                        log(s"WebSocket triggered ${logType.toUpperCase} log message")
                          .as {
                            WebSocketFrame.Text(s"Logged ${logType.toUpperCase} message")
                          }
                      }
                }
                .repeat
                .metered(1 second),
              input =>
                input
                  .evalMap {
                    case _: Close => logger.info[F]("WebSocket connection closed")

                    case frame =>
                      frame.data.decodeUtf8.toType[F, Throwable].flatMap { message =>
                        logger.info(s"Received: $message")
                      }
                  }
                  .productR(Stream.empty)
            )

        }
    }
  }

}
