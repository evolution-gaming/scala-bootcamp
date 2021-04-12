package com.evolutiongaming.bootcamp.tf.shopping

import cats.effect.{ExitCode, IO, IOApp}
import com.evolutiongaming.bootcamp.tf.shopping.clients._
import com.evolutiongaming.bootcamp.tf.shopping.routers.{CheckoutRouter, OrderRouter, RootRouter, ShoppingCartRouter}
import com.evolutiongaming.bootcamp.tf.shopping.services._

object ShoppingMain extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val paymentService = PaymentService[IO]
    for {
      shoppingCartService <- ShoppingCartService.of[IO]
      fileClient           = FileClient[IO]
      orderService         = OrderService[IO](fileClient)
      checkoutService      = CheckoutService[IO](shoppingCartService, paymentService, orderService)
      shoppingCartRouter   = ShoppingCartRouter(shoppingCartService)
      checkoutRouter       = CheckoutRouter(checkoutService)
      orderRouter          = OrderRouter(orderService)
      rootRoutes           = RootRouter(shoppingCartRouter, checkoutRouter, orderRouter)
      _                   <- ConsoleInterface(rootRoutes).repl
    } yield ExitCode.Success
  }

}
