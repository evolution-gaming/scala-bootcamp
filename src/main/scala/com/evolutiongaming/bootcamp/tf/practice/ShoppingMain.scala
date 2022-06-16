package com.evolutiongaming.bootcamp.tf.practice

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.bootcamp.tf.practice.clients.{FileClient, PaymentClient}
import com.evolutiongaming.bootcamp.tf.practice.effects.{Console, FromFuture}
import com.evolutiongaming.bootcamp.tf.practice.routers.{CheckoutRouter, OrderRouter, RootRouter, ShoppingCartRouter}
import com.evolutiongaming.bootcamp.tf.practice.services.{
  CheckoutService,
  OrderService,
  PaymentService,
  ShoppingCartService
}

import scala.concurrent.{ExecutionContextExecutor, Future}

object ShoppingMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    implicit val fromFutureIO: FromFuture[IO]               = FromFuture.lift[IO]

    val paymentService: PaymentService[IO]           = PaymentService[IO](PaymentClient())
    val paymentServiceFuture: PaymentService[Future] = paymentService.toUnsafe

    for {
      shoppingService <- ShoppingCartService.of[IO]
      orderService     = OrderService[IO](FileClient[IO])
      rootRouter       = RootRouter[IO](
                           ShoppingCartRouter(shoppingService),
                           CheckoutRouter(
                             CheckoutService[IO](
                               shoppingService,
                               paymentService,
                               orderService
                             )
                           ),
                           OrderRouter[IO](orderService)
                         )
      consoleInterface = ConsoleInterface[IO](
                           Console[IO],
                           rootRouter
                         )
      _               <- consoleInterface.repl
    } yield ExitCode.Success
  }
}
