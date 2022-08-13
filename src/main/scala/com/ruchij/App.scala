package com.ruchij

import cats.effect.{ExitCode, IO, IOApp, Sync}
import com.ruchij.config.ServiceConfiguration
import com.ruchij.services.health.HealthServiceImpl
import com.ruchij.types.Logger
import com.ruchij.web.Routes
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object App extends IOApp {
  private val logger = Logger[App.type]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      healthService = new HealthServiceImpl[IO](serviceConfiguration.buildInformation)

//      fiber <- logStream[IO, App.type](logger).metered(250 milliseconds).compile.drain.start

      _ <- EmberServerBuilder
        .default[IO]
        .withHttpWebSocketApp(webSocketBuilder => Routes(healthService, webSocketBuilder))
        .withIdleTimeout(1 hour)
        .withHost(serviceConfiguration.httpConfiguration.host)
        .withPort(serviceConfiguration.httpConfiguration.port)
        .build
        .use(_ => IO.never)

//      _ <- fiber.cancel
    } yield ExitCode.Success

  def logStream[F[_]: Sync, A](logger: Logger[A]): Stream[F, Unit] =
    Stream
      .emits[F, F[Unit]] {
        List(
          logger.info[F]("This is an INFO message"),
          logger.error[F]("This is an ERROR message", new Exception("This is an exception")),
          logger.debug[F]("This is a DEBUG message")
        )
      }
      .evalMap(identity)
      .repeat

}
