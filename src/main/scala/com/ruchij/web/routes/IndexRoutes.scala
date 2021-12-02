package com.ruchij.web.routes

import cats.data.OptionT
import cats.effect.kernel.Sync
import org.http4s.{HttpRoutes, Response, StaticFile}
import org.http4s.dsl.Http4sDsl

object IndexRoutes {
  val ExposedFileResources: Set[String] =  Set("index.html", "favicon.ico", "script.js", "styles.css")

  def apply[F[_]: Sync](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._

    HttpRoutes {
      case request @ GET -> Root =>
        StaticFile.fromResource("index.html", Some(request), preferGzipped = true)

      case request @ GET -> Root / fileName if ExposedFileResources.contains(fileName)  =>
        StaticFile.fromResource(fileName, Some(request), preferGzipped = true)

      case _ => OptionT.none[F, Response[F]]
    }
  }
}
