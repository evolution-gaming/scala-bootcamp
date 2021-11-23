package com.evolutiongaming.bootcamp.tf.practice.services

import cats.Monad
import cats.data.EitherT
import com.evolutiongaming.bootcamp.tf.practice.domain.UserId
import com.evolutiongaming.bootcamp.tf.practice.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.{Card, Payment}
import com.evolutiongaming.bootcamp.tf.practice.services.CheckoutService.CheckoutError

trait CheckoutService[F[_]] {
  def checkout(userId: UserId, card: Card): F[Either[CheckoutError, OrderId]]
//  def checkout(userId: UserId, card: Card): F[Option[OrderId]]

  //  def checkout(userId: UserId, card: Card): OptionT[F, OrderId] <- WRONG
  //  def checkout(userId: UserId, card: Card): EitherT[F, CheckoutError, OrderId] <- WRONG
}

object CheckoutService {

  sealed trait CheckoutError
  object CheckoutError {
    case object CartNotFound extends CheckoutError
  }

  def apply[F[_]: Monad](
    shoppingCartService: ShoppingCartService[F],
    paymentService: PaymentService[F],
    orderService: OrderService[F]
  ): CheckoutService[F] =
    (userId: UserId, card: Card) => {

      val eitherT: EitherT[F, CheckoutError, OrderId] = for {
        cart      <- EitherT.fromOptionF(shoppingCartService.get(userId), CheckoutError.CartNotFound: CheckoutError)
        paymentId <- EitherT.liftF(paymentService.process(Payment(userId, cart.total, card)))
        orderId   <- EitherT.liftF(orderService.create(userId, paymentId, cart.items, cart.total))
        _         <- EitherT.liftF(shoppingCartService.delete(userId))
      } yield orderId

      eitherT.value
    }
}
