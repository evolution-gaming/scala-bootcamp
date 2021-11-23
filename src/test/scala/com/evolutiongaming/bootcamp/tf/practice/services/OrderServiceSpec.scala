package com.evolutiongaming.bootcamp.tf.practice.services

import com.evolutiongaming.bootcamp.tf.practice.clients.FileClient
import com.evolutiongaming.bootcamp.tf.practice.domain.{Money, UserId}
import com.evolutiongaming.bootcamp.tf.practice.domain.order.{Order, OrderId}
import com.evolutiongaming.bootcamp.tf.practice.domain.payment.PaymentId
import com.evolutiongaming.bootcamp.tf.practice.effects.UUIDSupport
import com.evolutiongaming.bootcamp.tf.practice.services.OrderService.OrderServiceError
import io.circe.Decoder
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.{Currency, UUID}
import scala.util.{Success, Try}

class OrderServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {

  "OrderService # findOrThrow" should {

    "throw OrderNotFound in case if order not found" in new Scope {
      when(fileClient.read[List[Order]](any[String])(any[Decoder[List[Order]]])).thenReturn(Success(List.empty))
      orderService.findOrThrow(orderId).toEither shouldBe Left(OrderServiceError.OrderNotFound(orderId))
    }

    "return Order when it's found" in new Scope {
      private val order = Order(
        orderId,
        UserId(uuid),
        PaymentId("paymentId"),
        List.empty,
        Money(BigDecimal(100), Currency.getInstance("USD"))
      )
      when(fileClient.read[List[Order]](any[String])(any[Decoder[List[Order]]])).thenReturn(Success(List(order)))
      orderService.findOrThrow(orderId).toOption shouldBe Some(order)
    }
  }

  private trait Scope {

    val fileClient: FileClient[Try]            = mock[FileClient[Try]]
    implicit val UUIDSupport: UUIDSupport[Try] = mock[UUIDSupport[Try]]

    val orderService: OrderService[Try] = OrderService[Try](fileClient)

    val uuid: UUID       = UUID.fromString("b02308c4-b7dc-4f29-9a5f-d9c90c981a40")
    val orderId: OrderId = OrderId(uuid)
  }
}
