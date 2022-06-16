package com.evolutiongaming.bootcamp.tf.practice.clients

import cats.effect.Sync
import com.evolutiongaming.bootcamp.tf.practice.utils.FileUtils
import io.circe._
import io.circe.parser.decode
import io.circe.syntax._

trait FileClient[F[_]] {
  def write[A: Encoder](fileName: String, value: A): F[Unit]
  def read[A: Decoder](fileName: String): F[A]
}

object FileClient {

  def apply[F[_]: Sync]: FileClient[F] =
    new FileClient[F] {

      def write[A: Encoder](fileName: String, value: A): F[Unit] =
        Sync[F].delay {
          FileUtils.writeToFile(fileName, value.asJson.toString())
        }

      def read[A: Decoder](fileName: String): F[A] =
        Sync[F].fromEither {
          decode[A](FileUtils.readFromFile(fileName))
        }
    }
}
