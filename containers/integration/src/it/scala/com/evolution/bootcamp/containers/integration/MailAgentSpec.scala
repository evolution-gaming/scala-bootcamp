package com.evolution.bootcamp.containers.integration

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.verbs.ShouldVerb

class MailAgentSpec extends AnyFlatSpec with ShouldVerb {
  "MailAgent" should "send example email in configured environment" in {
    val ma = new MailAgent()
    ma.sendExampleEmail("Test Example", "Example body.") should be (Right(()))
  }
}
