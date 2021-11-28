package com.ruchij.test.mixins.io

import cats.effect.IO
import cats.effect.kernel.Async
import com.ruchij.test.mixins.MockedRoutes

trait MockedRoutesIO extends MockedRoutes[IO] {
  override val async: Async[IO] = IO.asyncForIO
}
