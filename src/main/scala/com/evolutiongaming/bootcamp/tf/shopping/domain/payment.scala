package com.evolutiongaming.bootcamp.tf.shopping.domain

import com.evolutiongaming.bootcamp.tf.shopping.domain.card.Card
import com.evolutiongaming.bootcamp.tf.shopping.domain.money.Money
import com.evolutiongaming.bootcamp.tf.shopping.domain.user.UserId
import io.circe.generic.JsonCodec

object payment {

  final case class PaymentId(id: String)
  final case class Payment(userId: UserId, amount: Money, card: Card)

}
