package com.evolutiongaming.bootcamp.tf.practice.services

import cats.Id
import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}
import com.evolutiongaming.bootcamp.tf.practice.domain.cart.CartTotal
import com.evolutiongaming.bootcamp.tf.practice.domain.order.OrderId
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.{Card, Payment, PaymentId}
import com.evolutiongaming.bootcamp.tf.practice.services.CheckoutService.CheckoutError
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.{Currency, UUID}

class CheckoutServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "CheckoutService # checkout" should {

    "return CheckoutError.CartNotFound in case of missing user cart" in new Scope {
      when(shoppingCartService.get(userId)).thenReturn(None)
      checkoutService.checkout(userId, card) shouldBe Left(CheckoutError.CartNotFound)
    }

    "process payment, create order, delete cart and return OrderId" in new Scope {

      private val cartTotal = CartTotal(List.empty, Money(BigDecimal(1), Currency.getInstance("USD")))
      private val paymentId = PaymentId("paymentId")
      private val orderId   = OrderId(uuid)

      when(shoppingCartService.get(userId)).thenReturn(Some(cartTotal))
      when(paymentService.process(Payment(userId, cartTotal.total, card))).thenReturn(paymentId)
      when(orderService.create(userId, paymentId, List.empty, cartTotal.total)).thenReturn(orderId)
      when(shoppingCartService.delete(userId)).thenReturn(())

      checkoutService.checkout(userId, card) shouldBe Right(orderId)
    }
  }

  private trait Scope {

    val shoppingCartService: ShoppingCartService[Id] = mock[ShoppingCartService[Id]]
    val paymentService: PaymentService[Id]           = mock[PaymentService[Id]]
    val orderService: OrderService[Id]               = mock[OrderService[Id]]

    val checkoutService: CheckoutService[Id] = CheckoutService[Id](
      shoppingCartService,
      paymentService,
      orderService
    )

    val uuid: UUID     = UUID.fromString("b02308c4-b7dc-4f29-9a5f-d9c90c981a40")
    val userId: UserId = UserId(uuid)
    val card: Card     = Card("cardNumber", "cvv")
  }
}
