package com.evolutiongaming.bootcamp.testing2

import com.evolutiongaming.bootcamp.testing2.hal9000.HAL9000
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues
import scala.annotation.nowarn

// *Introduction*
//
// If we cannot avoid doing the feature and we cannot make sure compiler or
// static checker tests for the feature to work correctly, we have to rely
// on unit testing. Unit testing is extremely powerful for Scala these days.
//
// I.e. besides just checking some scenario as we used to do years ago,
// one can specify the properties and the laws the program should adhere
// to (https://www.scalacheck.org), try to break the tests using mutation
// testing (https://github.com/stryker-mutator/stryker4s), or even prove
// the software is working correctly: https://www.youtube.com/watch?v=7w4KC6i9Yac.
//
// The main issue here is to not overdo. If we see that some part is not
// covered by unit tests, we might first ask ourselves if lower layer would
// be a better place to check the property, or if it is cost-effective to test
// it. I.e. testing and maintaining the tests should be lower than cost of
// the bug multiplied by probability of it to happen. Otherwise it makes no
// sense to test for it.
//
// The good developer will sense where unit tests are needed and will write
// them himself, but a lot of guys prefer to use coverage measuring tools to
// spot the parts which are not covered. The typical tool Scala guys would
// use for it is http://scoverage.org/. Note, that we do not strive for
// 100% coverage anymore these days, because of lower levels serving us,
// and other reasons, but it is a really cool way to discover the blind
// spots.
//
// Note, that one can use it to measure coverage of other type of tests
// such as integration (including end-to-end tests) and manual tests. The way
// it works, is that injects a piece of code into the application, which
// does the recording while the app works. It may affect the performance and
// stability of the app, and, generally, one do not want this to be done in
// production or production-like environments.
//
// We have thousands of unit tests in our every app. The most typical usage
// is testing business logic of the app, but there are a lot more various use
// cases. A lot of guys are choosing to use behavior driven way of writing
// their specification, but there is no single style adopted in the company.
// Most used unit testing framework is ScalaTest (http://www.scalatest.org),
// but it is not the only one.
//
// Besides we use contract driven test here such as https://docs.pact.io/,
// but the adoption is relatively small for now.


// *Structure*
//
// In Java world one of the most used build tools is called Maven. The idea
// of Maven is to folow "Convention over Configuration" principle.
//
// It means that for most of the projects you just put the stuff where
// it belongs and then it automagically compiles and builds.
//
// The most popular build tool in Scala world uses the same idea.
// You just put your code into `src/main/scala` and you tests into
// `src/main/test` and it compiles and runs everything automatically
// without a configuration.
//
// The classes under test for this workshop are stored in `src/main/scala/testing2`
// and the tests themselves are stored in `src/test/scala/testing2`.


// *Excersise 1*
//
// Your IDE (IntelliJ or Visual Studio Code + Metals) also understands this
// convention and already knows how to run the tests using popular testing
// frameworks directly from IDE. For some advanced cases you might want to use
// sbt directly though.
//
// Let's try to run all the tests for the bootcamp using `sbt`. Run `sbt test`
// inside of `scala-bootcamp` directory.
//
// > sbt test
//
// Some tests fails, some test pass, but it takes quite a time to start them.
// It takes at least several seconds on my powerful laptop.
//
// Why? This is because `sbt` is JVM application and takes considerable time
// to load. How much the tests took in total on your computer?
//
// Latest `sbt` versions (starting from `1.4.0`) have a native client
// to avoid a startup time. Before that professional Scala developers using
// `sbt` preferred to run it interactive mode.
//
// Let's run `sbt` in interactive mode by running `sbt` without any parameters.
//
// > sbt
//
// Now type in `test` and press ENTER. Note how long it took to start runing
// the tests.
//
// sbt:scala-bootcamp> test
//
// Now try again and again. Did the time change?
//
// The reason the time changes is the way JVM works. It uses Just-In-Time
// compiler, which is able to notice so-called hot spots in the code and
// compile them into the native code, or even recompile them using the
// information found during the runtime making it smarter than so called
// Ahead-Of-Time compilers. That what makes JVM so powerful.
//
// Saying that, there is also AOT compiler shipping with JVM these days, so
// you can actually compile the Java or Scala code into machine code and
// enjoy the very fast startup time.
//
// Can we make it faster if we only want to run part of the tests? There
// is another command in `sbt` which supports it. It is called `testOnly`
// Let's try to run the tests related to this workshop only.
//
// sbt:scala-bootcamp> testOnly *testing2*
//
// There is also a command which only runs the tests failing a previous
// run or the tests which have their dependencies changed. Try it out.
// Did you have less tests running this time?
//
// sbt:scala-bootcamp> testQuick
//

// *Excercise 2*
//
// There are several popular testing libraries for Scala in existence. Arugably,
// the most popular and one of most flexible is called `ScalaTest`. One of the
// reasons why it is so popular is because it supports a lot of testing styles
// and DSLs.
//
// ScalaTest author is Bill Venners, the co-author of "Programming in Scala" book.
//
// Other popular libraries are the following:
//
// `Specs2` - one of the oldest libraries with opinionated Cucubmer-like DSL,
//            made by Eric Torreborre from Zalando.
//
// `MUnit` - lightweight tesing library inspired by JUnit by the author of Metals,
//           Ólafur Páll Geirsson from Twitter.
//
// `Weaver-test` - tailored for integration tessting by Olivier Mélois from
//                 Disney Streaming.
//
// `utest` - simple testing library by Li Haoyi from Databricks, authors of
//           Apache Spark. He just published a new book on Scala advocating
//           the simple Python-like coding style in Scala.
//
// So, what are the styles ScalaTest support? You can find them on the following
// page: https://www.scalatest.org/user_guide/selecting_a_style
//
// One of the most popular is `FreeSpec` which allows one to write your test cases in a free form.
//
// Run the following suite using the command below:
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise2Spec
//
// Now change the suite so it outputs the following code instead,
// while keeping all the asserts.
//
// [info] Excersise1Spec:
// [info] calculator
// [info] - enters the number correctly
// [info]   fails if incorrect number is pressed
// [info]   does nothing
// [info]   - when you just repeat pressing `=`
//
// Hint: you can recall the preivously run `sbt` commands by pressing
// up arrow on a keyboard.
//
// Hint: you can make the development process even more convinient by
// forcing `sbt` to monitor the changes you do to the files and rerun
// the tests automatically by adding `~` before the command. It also
// works on other `sbt` commands.
//
// sbt:scala-bootcamp> ~testOnly *testing2.Excersise2Spec
//
// Now break one of the tests, i.e. change `calculator.enter(1)` to
// `calculator.enter(2)`. Observe the output. How did Scala manage
// to output such a thing?
class Excersise2Spec extends AnyFreeSpec {

  "calculator" - {
    "enters the number correctly" in {
      val calculator = Calculator()
      assert(calculator.enter(1) == Right(Calculator(1, 0, None)))
      assert(calculator.enter(7) == Right(Calculator(7, 0, None)))
      assert(calculator.enter(12) == Left("digit out of range"))
    }
    "does nothing" - {
      "when you just repeat pressing `=`" in {
        val calculator = Calculator()
        assert(calculator.calculate.calculate.calculate.calculate == calculator)
      }
    }
  }

}
// *Excercise 3*
//
// Another popular way to write tests is `WordSpec` as it pushes a very strict
// BDD style of writing the tests to the team.
//
// Find it in the following page and rewrite the test from Excersise 2 to
// this new style:
// https://www.scalatest.org/user_guide/selecting_a_style
//
// Run it using sbt again:
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise3Spec
//
class Excersise3Spec extends AnyWordSpec {
}

// *Note*
//
// Which style do you like more? Are you ready to argue with your colleagues
// for several days over the best style? Scala developers used to fight about
// it a lot in early days. Not anymore though

// *Excercise 4*
//
// What does `assert` word actually do? Can you write it differently?
//
// Both `ScalaTest` and `Specs2` support writing so called matchers which
// make it easier to write human readable tests.
//
// The detailed documentation could be found here:
// https://www.scalatest.org/user_guide/using_matchers
//
// Rewrite asserts to `should be` matcher and run it using sbt again:
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise4Spec
//
// Now break one of the tests, i.e. change `calculator.enter(1)` to
// `calculator.enter(2)`. Observe the output. Do you like the input?
// How does it compare to what you seen in `Excersise1`?
class Excersise4Spec extends AnyFreeSpec with Matchers {

  "calculator" - {
    "enters the number correctly" in {
      val calculator = Calculator()
      calculator.enter(1) should be (Right(Calculator(1, 0, None)))
      assert(calculator.enter(7) == Right(Calculator(7, 0, None)))
      assert(calculator.enter(12) == Left("digit out of range"))
    }
    "does nothing" - {
      "when you just repeat pressing `=`" in {
        val calculator = Calculator()
        assert(calculator.calculate.calculate.calculate.calculate == calculator)
      }
    }
  }

}

// *Excercise 5*
//
// This test, arguably, now looks a bit more readable. Can we get rid
// of these verbose `Right` and `Left` words?
//
// Actually we can and there is whole construct for that in ScalaTest:
// https://www.scalatest.org/user_guide/using_EitherValues
//
// There is also similar construct for `Option`, `PartialFunction` etc.
//
// Let's rewrite asserts from the first excersise (or previous excersise
// if you prefer so) to the new way. There is one line already rewritten
// so you can have an example.
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise5Spec
//
// Now break one of the tests, i.e. change `calculator.enter(1)` to
// `calculator.enter(900)`. Observe the output. Do you like the input?
// How does it compare to what you seen in Excersise 2?
@nowarn
class Excersise5Spec extends AnyFreeSpec with EitherValues {

  "calculator" - {
    "enters the number correctly" in {
      val calculator = Calculator()
      assert(calculator.enter(1).right.value == Calculator(1, 0, None))
      assert(calculator.enter(7) == Right(Calculator(7, 0, None)))
      assert(calculator.enter(12) == Left("digit out of range"))
    }
    "does nothing" - {
      "when you just repeat pressing `=`" in {
        val calculator = Calculator()
        assert(calculator.calculate.calculate.calculate.calculate == calculator)
      }
    }
  }

}

// *Excercise 6*
//
// To quote (Li Haoyi, author of utest):
//
// > uTest tries to provide things that every developer needs, in their minimal,
// > essential form. It intentionally avoids redundant/unnecessary features or
// > syntaxes that bloat the library and make it harder to developers to pick up,
// > which I find to be common in other popular testing libraries like Scalatest
// > or Specs2:
// >
// > - Fluent English-like code: matchers like `shouldBe` or `should not be` or
// > `mustbe_==` don't really add anything, and it doesn't really matter whether
// > you name each test block using `should`, `when`, `can`, `must`,
// > `feature("...")` or it `should "..."`.
// >
// > - Multiple redundant ways of defining test suites, individual tests and
// > - blocks of related tests
// >
// > Legacy code, like ScalaTests time package, now obsolete with the introduction
// > of scala.concurrent.duration.
// >
// > While uTest has and will continue to slowly grow and add more features,
// > it is unlikely that it will ever reach the same level of complexity that
// > other testing libraries are currently at.
// https://github.com/lihaoyi/utest#why-utest
//
// ScalaTest authors took the critisism seriously and made large part of the
// features pluggable. The appearance of lightweight libraries such as utest
// also made these features less popular. You will encounter them, regardless,
// when writing the code, and, who knows, may be it will make your QA engineers
// happier if you make the code more readable, so it is good to be familiar with
// them.
//
// Rewrite the suite from Excersise 2 to `FunSuite` style, the same style
// used by lightweight libraries and also supported by ScalaTest. Run it using
// sbt again:
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise6Spec
//
class Excersise6Spec extends AnyFunSuite {

  test("name of the test 1") {
    // here goes your test 1
  }
  test("name of the test 2") {
    // here goes your test 2
  }

}

// *Excercise 7*
//
// As you may have noticed, there is a convention to put the tests for the
// classes in a package located in `src/main/scala/interesting/package` under
// the same directory structure also in `/src/test/scala/interesting/package`.
//
// It makes the tests easier to find and relate to the existing code. But
// there is also another reason for that. Let's find out the reason by
// making a test for `testing2.hal9000.HAL9000` class.
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise7Spec
//
class Excersise7Spec extends AnyFunSuite {

  test("HAL 9000 multiplies numbers correctly") {
    // assert(HAL9000.twice(7) == 14)
  }

}

// *Excercise 8*
//
// Did you notice another method in HAL 9000? It fails! Can we test it?
//
// Write a test using one of the methods found here:
// https://www.scalatest.org/user_guide/using_assertions#expectedExceptions
//
// There is also a special matcher for that, if you want to use them:
// https://www.scalatest.org/user_guide/using_matchers#expectedExceptions
//
// sbt:scala-bootcamp> testOnly *testing2.Excersise8Spec
//
class Excersise8Spec extends AnyFunSuite {

  test("HAL 9000 behaves as expected when asked to open the door") {
  }

}