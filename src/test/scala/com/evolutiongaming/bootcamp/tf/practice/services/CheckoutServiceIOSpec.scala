package com.evolutiongaming.bootcamp.tf.practice.services

import cats.effect.IO
import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.CartTotal
import com.evolutiongaming.bootcamp.tf.practice.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.{Card, Payment, PaymentId}
import com.evolutiongaming.bootcamp.tf.practice.services.CheckoutService.CheckoutError
import org.mockito.MockitoSugar
import org.scalatest.funspec.AsyncFunSpec

import java.util.{Currency, UUID}

class CheckoutServiceIOSpec extends AsyncFunSpec with MockitoSugar {

  describe("CheckoutService # checkout") {

    it("should return CheckoutError.CartNotFound in case of missing user cart") {
      val scope = new Scope {}
      import scope._

      when(shoppingCartService.get(userId)).thenReturn(IO.pure(None))
      checkoutService.checkout(userId, card).unsafeToFuture().map { result =>
        assert(result == Left(CheckoutError.CartNotFound))
      }
    }

    it("process payment, create order, delete cart and return OrderId") {
      val scope = new Scope {}
      import scope._

      val cartTotal = CartTotal(List.empty, Money(BigDecimal(1), Currency.getInstance("USD")))
      val paymentId = PaymentId("paymentId")
      val orderId   = OrderId(uuid)

      when(shoppingCartService.get(userId)).thenReturn(IO.pure(Some(cartTotal)))
      when(paymentService.process(Payment(userId, cartTotal.total, card))).thenReturn(IO.pure(paymentId))
      when(orderService.create(userId, paymentId, List.empty, cartTotal.total)).thenReturn(IO.pure(orderId))
      when(shoppingCartService.delete(userId)).thenReturn(IO.unit)

      checkoutService.checkout(userId, card).unsafeToFuture().map { result =>
        assert(result == Right(orderId))
      }
    }
  }

  private trait Scope {

    val shoppingCartService: ShoppingCartService[IO] = mock[ShoppingCartService[IO]]
    val paymentService: PaymentService[IO]           = mock[PaymentService[IO]]
    val orderService: OrderService[IO]               = mock[OrderService[IO]]

    val checkoutService: CheckoutService[IO] = CheckoutService[IO](
      shoppingCartService,
      paymentService,
      orderService
    )

    val uuid: UUID     = UUID.fromString("b02308c4-b7dc-4f29-9a5f-d9c90c981a40")
    val userId: UserId = UserId(uuid)
    val card: Card     = Card("cardNumber", "cvv")
  }
}
