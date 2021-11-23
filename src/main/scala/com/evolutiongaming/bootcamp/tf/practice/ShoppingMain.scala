package com.evolutiongaming.bootcamp.tf.practice

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.bootcamp.tf.practice.clients.{FileClient, PaymentClient}
import com.evolutiongaming.bootcamp.tf.practice.effects.FromFuture
import com.evolutiongaming.bootcamp.tf.practice.routers.{CheckoutRouter, OrderRouter, RootRouter, ShoppingCartRouter}
import com.evolutiongaming.bootcamp.tf.practice.services.{CheckoutService, OrderService, PaymentService, ShoppingCartService}

import scala.concurrent.{ExecutionContextExecutor, Future}

object ShoppingMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val fromFutureIO: FromFuture[IO]               = FromFuture.lift[IO]

    val service: PaymentService[IO] = PaymentService[IO](PaymentClient())
    val a: PaymentService[Future] = service.toUnsafe

    for {
      shoppingCartService <- ShoppingCartService.of[IO]
      paymentService       = service
      orderService         = OrderService[IO](FileClient[IO])
      checkoutService      = CheckoutService[IO](
                               shoppingCartService,
                               paymentService,
                               orderService
                             )
      rooRouter            = RootRouter[IO](
                               ShoppingCartRouter[IO](shoppingCartService),
                               CheckoutRouter[IO](checkoutService),
                               OrderRouter[IO](orderService)
                             )
      _                   <- ConsoleInterface(rooRouter).repl
    } yield ExitCode.Success
  }
}
