package com.ruchij.test.mixins

import cats.effect.kernel.Async
import cats.implicits.toFunctorOps
import com.ruchij.services.health.HealthService
import com.ruchij.web.Routes
import org.http4s.HttpApp
import org.http4s.test.TestWebSocketBuilder
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest

trait MockedRoutes[F[_]] extends MockFactory with OneInstancePerTest {

  val healthService: HealthService[F] = mock[HealthService[F]]

  val async: Async[F]

  def createRoutes(): F[HttpApp[F]] =
    toFunctorOps(TestWebSocketBuilder.create(async, async))(async)
      .map(webSocketBuilder => Routes(healthService, webSocketBuilder)(async))

}
