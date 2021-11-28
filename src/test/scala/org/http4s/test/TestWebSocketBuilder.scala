package org.http4s.test

import cats.Applicative
import cats.effect.Unique
import cats.implicits._
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketContext
import org.typelevel.vault.Key

object TestWebSocketBuilder {
  def create[F[_]: Applicative: Unique]: F[WebSocketBuilder2[F]] =
    Key.newKey[F, WebSocketContext[F]].map { webSocketKey => WebSocketBuilder2(webSocketKey) }
}
