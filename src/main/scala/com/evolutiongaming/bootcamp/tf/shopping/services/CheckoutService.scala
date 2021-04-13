package com.evolutiongaming.bootcamp.tf.shopping.services

import cats.Monad
import cats.data.EitherT
import com.evolutiongaming.bootcamp.tf.shopping.domain.card.Card
import com.evolutiongaming.bootcamp.tf.shopping.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.shopping.domain.payment.Payment
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId

sealed trait CheckoutError

object CheckoutError {
  case object CartNotFound extends CheckoutError
}

trait CheckoutService[F[_]] {
  def checkout(userId: UserId, card: Card): F[Either[CheckoutError, OrderId]]
}

object CheckoutService {

  def apply[F[_]: Monad](
    shoppingCart: ShoppingCartService[F],
    paymentService: PaymentService[F],
    orderService: OrderService[F]
  ): CheckoutService[F] = (userId: UserId, card: Card) => {
    val result: EitherT[F, CheckoutError, OrderId] = for {
      cart <- EitherT.fromOptionF(shoppingCart.get(userId), CheckoutError.CartNotFound: CheckoutError)
      paymentId <- EitherT.liftF(
        paymentService.process(
          Payment(userId, cart.total, card)
        )
      )
      orderId <- EitherT.liftF(
        orderService.create(
          userId,
          paymentId,
          cart.items,
          cart.total
        )
      )
      _ <- EitherT.liftF(shoppingCart.delete(userId))
    } yield orderId

    result.value
  }

}
