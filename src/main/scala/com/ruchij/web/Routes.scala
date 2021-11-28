package com.ruchij.web

import cats.effect.kernel.Async
import cats.implicits._
import com.ruchij.services.health.HealthService
import com.ruchij.web.middleware.{ExceptionHandler, NotFoundHandler}
import com.ruchij.web.routes.{HealthRoutes, IndexRoutes, LoggingRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.middleware.GZip
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.{HttpApp, HttpRoutes}

object Routes {
  def apply[F[_]: Async](healthService: HealthService[F], webSocketBuilder2: WebSocketBuilder2[F]): HttpApp[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: HttpRoutes[F] =
      Router(
        "/service" -> HealthRoutes[F](healthService),
        "/log" -> LoggingRoutes[F](webSocketBuilder2)
      ) <+> IndexRoutes[F]

    GZip {
      ExceptionHandler {
        NotFoundHandler(routes)
      }
    }
  }
}
