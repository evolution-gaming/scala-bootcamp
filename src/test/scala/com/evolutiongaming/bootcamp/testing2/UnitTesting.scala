package com.evolutiongaming.bootcamp.testing2

import cats.Monad
import cats.data.State
import cats.syntax.all._
import com.evolutiongaming.bootcamp.testing2.hal9000.HAL9000
import java.util.concurrent.atomic.AtomicReference
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.annotation.nowarn
import scala.concurrent.Future
import scala.concurrent.Promise

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
// of Maven is to follow "Convention over Configuration" principle.
//
// It means that for most of the projects you just put the stuff where
// it belongs and then it automagically compiles and builds.
//
// The most popular build tool in Scala world uses the same idea.
// You just put your code into `src/main/scala` and you tests into
// `src/test/scala` and it compiles and runs everything automatically
// without a configuration.
//
// The classes under test for this workshop are stored in `src/main/scala/testing2`
// and the tests themselves are stored in `src/test/scala/testing2`.


// *Exercise 1*
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

// *Exercise 2*
//
// There are several popular testing libraries for Scala in existence. Arguably,
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
// `MUnit` - lightweight testing library inspired by JUnit by the author of Metals,
//           Ólafur Páll Geirsson from Twitter.
//
// `Weaver-test` - tailored for integration testing by Olivier Mélois from
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
// sbt:scala-bootcamp> testOnly *testing2.Exercise2Spec
//
// Now change the suite so it outputs the following code instead,
// while keeping all the asserts.
//
// [info] Exercise1Spec:
// [info] calculator
// [info] - enters the number correctly
// [info]   fails if incorrect number is pressed
// [info]   does nothing
// [info]   - when you just repeat pressing `=`
//
// Hint: you can recall the previously run `sbt` commands by pressing
// up arrow on a keyboard.
//
// Hint: you can make the development process even more convenient by
// forcing `sbt` to monitor the changes you do to the files and rerun
// the tests automatically by adding `~` before the command. It also
// works on other `sbt` commands.
//
// sbt:scala-bootcamp> ~testOnly *testing2.Exercise2Spec
//
// Now break one of the tests, i.e. change `calculator.enter(1)` to
// `calculator.enter(2)`. Observe the output. How did Scala manage
// to output such a thing?
class Exercise2Spec extends AnyFreeSpec {

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
// *Exercise 3*
//
// Another popular way to write tests is `WordSpec` as it pushes a very strict
// BDD style of writing the tests to the team.
//
// Find it in the following page and rewrite the test from Exercise 2 to
// this new style:
// https://www.scalatest.org/user_guide/selecting_a_style
//
// Run it using sbt again:
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise3Spec
//
class Exercise3Spec extends AnyWordSpec {
}

// *Note*
//
// Which style do you like more? Are you ready to argue with your colleagues
// for several days over the best style? Scala developers used to fight about
// it a lot in early days. Not anymore though...

// *Exercise 4*
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
// sbt:scala-bootcamp> testOnly *testing2.Exercise4Spec
//
// Now break one of the tests, i.e. change `calculator.enter(1)` to
// `calculator.enter(2)`. Observe the output. Do you like the input?
// How does it compare to what you seen in `Exercise1`?
class Exercise4Spec extends AnyFreeSpec with Matchers {

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

// *Exercise 5*
//
// This test, arguably, now looks a bit more readable. Can we get rid
// of these verbose `Right` and `Left` words?
//
// Actually we can and there is whole construct for that in ScalaTest:
// https://www.scalatest.org/user_guide/using_EitherValues
//
// There is also similar construct for `Option`, `PartialFunction` etc.
//
// Let's rewrite asserts from the first Exercise (or previous Exercise
// if you prefer so) to the new way. There is one line already rewritten
// so you can have an example.
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise5Spec
//
// Now break one of the tests, i.e. change `calculator.enter(1)` to
// `calculator.enter(900)`. Observe the output. Do you like the input?
// How does it compare to what you seen in Exercise 2?
@nowarn
class Exercise5Spec extends AnyFreeSpec with EitherValues {

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

// *Exercise 6*
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
// ScalaTest authors took the criticism seriously and made large part of the
// features pluggable. The appearance of lightweight libraries such as utest
// also made these features less popular. You will encounter them, regardless,
// when writing the code, and, who knows, may be it will make your QA engineers
// happier if you make the code more readable, so it is good to be familiar with
// them.
//
// Rewrite the suite from Exercise 2 to `FunSuite` style, the same style
// used by lightweight libraries and also supported by ScalaTest. Run it using
// sbt again:
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise6Spec
//
class Exercise6Spec extends AnyFunSuite {

  test("name of the test 1") {
    // here goes your test 1
  }
  test("name of the test 2") {
    // here goes your test 2
  }

}

// *Exercise 7*
//
// As you may have noticed, there is a convention to put the tests for the
// classes in a package located in `src/main/scala/interesting/package` under
// the same directory structure also in `src/test/scala/interesting/package`.
//
// It makes the tests easier to find and relate to the existing code. But
// there is also another reason for that. Let's find out the reason by
// making a test for `testing2.hal9000.HAL9000` class.
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise7Spec
//
class Exercise7Spec extends AnyFunSuite {

  test("HAL 9000 multiplies numbers correctly") {
    // assert(HAL9000.twice(7) == 14)
  }

}

// *Exercise 8*
//
// Did you notice another method in HAL 9000? It fails! Can we test it?
//
// Write a test using one of the methods found here:
// https://www.scalatest.org/user_guide/using_assertions#expectedExceptions
//
// There is also a special matcher for that, if you want to use them:
// https://www.scalatest.org/user_guide/using_matchers#expectedExceptions
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise8Spec
//
class Exercise8Spec extends AnyFunSuite {

  test("HAL 9000 behaves as expected when asked to open the door") {
  }

}

// *Exercise 9*
//
// It is the best to make the code self-documenting and readable. If we cannot,
// we are trying to make the tests readable. If we cannot achieve test to be
// readable, we can add the clues into tests.
//
// HAL 9000 goes crazy about his mission if two registers do not match.
// Unfortunately astronauts do not know about it. Save the astronauts
// by adding a clue to the test below according to the following document:
//
// https://www.scalatest.org/user_guide/using_assertions#gettingAClue
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise9Spec
//
class Exercise9Spec extends AnyFunSuite {

  test("HAL9000") {
    assert(HAL9000.register1 == HAL9000.register2)
  }

}

// *Exercise 10*
//
// Let's take a more serious example now. Implement a service and
// the tests for the service. Make sure you log the important steps using
// logging service.
//
// Note that you do not need too have the actual repository for the
// implementation. It is enough to have an interface with the methods
// and _inject_ it into the service.
//
// This pattern is sometimes called dependency injection because you inject
// repository versus calling it directly (in other object) and is able to
// change the implementation later.
//
object Exercise10 {

  case class Player(id: String, name: String, email: String, score: Int)
  trait PlayerRepository {
    def byId(id: String): Option[Player]
    def all: List[Player]
    def update(player: Player): Unit
    def delete(id: String): Unit
  }
  trait Logging {
    def info(message: String): Unit
  }

  trait PlayerService {

    /** Deletes all the players with score lower than minimum.
      *
      * @param miniumumScore the minimum score the player stays with.
      */
    def deleteWorst(minimumScore: Int): Unit

    /** Adds bonus points to score to all existing players
      *
      * @param bonus the bonus points to add to the players.
      */
    def celebrate(bonus: Int): Unit

  }
  object PlayerService {

    /** Creates a new service working with existing repository */
    def apply(repository: PlayerRepository, logging: Logging): PlayerService = new PlayerService {

      // NOTE: We do not have a returned type annotation and documentation here, why?
      def deleteWorst(minimumScore: Int) = ???
      def celebrate(bonus: Int) = ???

    }

  }

}
// As usual, you can run the following test suite using sbt:
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise10Spec
//
// You might want to construct a stub or a mock for repository to make
// the test possible.
//
// ScalaTest has a good documentation on how to do mocks with various mocking
// frameworks. This project has Mockito enabled:
// https://www.scalatest.org/user_guide/testing_with_mock_objects#mockito
//
// In this exercise you are welcome to choose your own strategy, but,
// in general, the author recommends to avoid mocks altogether.
//
// Mocks are very powerful, and often create an urge to cover everything with
// mocks. The problem is with mockist style tests that they often unit test
// nothing, but just do the "double accounting" of the existing code.
//
// We could, as well, written all the code twice. It is not a useless exercise,
// because you are noticing various issues with original code while writing it
// twice, but it has own drawbacks. Real, or stub based tests (in contrast to
// mocks) tend to test real constraints, you need to think before writing them,
// and they survive refactoring much better.
//
// You can read more here:
// https://martinfowler.com/articles/mocksArentStubs.html
//
// Bonus question: do we need to test logging?
//
class Exercise10Spec extends AnyFunSuite {

  import Exercise10._

  test("PlayerService.deleteWorst works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService(repository, logging)

    // perform the test
    service.deleteWorst(???)

    // validate the results
    assert(???)
  }

  test("PlayerService.celebrate works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService(repository, logging)

    // perform the test
    service.celebrate(???)

    // validate the results
    assert(???)
  }

}

// *Exercise 11*
//
// Do you see the copy-pasted code in Exercise 11? We create `repository` etc.
// twice. Let's create a fixture for that.
//
// See also: https://www.scalatest.org/user_guide/sharing_fixtures

// *Exercise 12*
//
// What if the code is asynchronous? Let's implement the same service in
// asynchronous way and write the tests for it.
//
// Hint: modern Scala developers rarely use `Future`. They use `IO` or similar
// construct instead which provides a lot of benefits and is more convenient to
// use. We are only using `Future` to keep it simple. Do not use it at home.
//
// Just replacing `Future` by `IO` will work most of the time (and here!). We
// will tell you more about `IO` on another lecture. You can also try
// self-education here: https://typelevel.org/cats-effect/datatypes/io.html.
//
// Hint: seriously, forget about `Future` after this exercise.
//
object Exercise12 {

  // We do not need to know how to create `Future` to write the implementation,
  // thanks to dependency injection, but we might want to know how to combine them.
  // Combining futures only work if you have implicit execution context in scope.
  // It is not required to combine `IO` objects, and that is one of the reasons
  // to use them instead.
  import scala.concurrent.ExecutionContext.Implicits.global

  // 1. The type wrapped by `Future` could be changed by using `map`:
  def dogs: Future[Int] = ???
  def message1: Future[String] = dogs map { dogs =>
    s"We have $dogs of dogs"
  }
  // 2. Two `Future` classes could be combined by `flatMap`:
  def cats: Future[Int] = ???
  def message2: Future[String] = dogs flatMap { dogs =>
    cats map { cats =>
      s"We have $dogs of dogs and $cats of cats"
    }
  }
  // 3. We can use `for...yield` notation to avoid nesting:
  def message3: Future[String] = for {
    dogs <- dogs
    cats <- cats
  } yield s"We have $dogs of dogs and $cats of cats"

  // Later, for tests, you will need to know how to create the features for your
  // stubs.
  //
  // The simplest is just to wrap your code into `Future { ... }`.
  // It will start executing in a different thread as soon as the code is called.
  def future1: Future[Int] = Future { 7 }
  //
  // Another way is to create it using a `Promise`.
  // It will not start executing anything and will complete when the promise is
  // fulfilled.
  //
  val promise: Promise[Int] = Promise()
  def future2: Future[Int] = promise.future
  //
  // Now let complete our Future:
  promise.success(7)
  //
  // Besides that, we will have to have a place to store stuff in our stubs as
  // using `var` from multiple threads is not safe. We will use `AtomicReference`
  // for that here. It will be explained in next lecture in details, we will just
  // accept the construct as it is for now.
  //
  // Create the storage place with default value:
  val storage: AtomicReference[List[Int]] = new AtomicReference(List(1, 2, 3))
  // Set the value to another one:
  storage.set(List(5, 6, 7))
  // Get the current value from the storage:
  val list = storage.get()

  case class Player(id: String, name: String, email: String, score: Int)
  trait PlayerRepository {
    def byId(id: String): Future[Option[Player]]
    def all: Future[List[Player]]
    def update(player: Player): Future[Unit]
    def delete(id: String): Future[Unit]
  }
  trait Logging {
    def info(message: String): Future[Unit]
  }

  trait PlayerService {

    /** Deletes all the players with score lower than minimum.
      *
      * @param miniumumScore the minimum score the player stays with.
      */
    def deleteWorst(minimumScore: Int): Future[Unit]

    /** Adds bonus points to score to all existing players
      *
      * @param bonus the bonus points to add to the players.
      */
    def celebrate(bonus: Int): Future[Unit]

  }
  object PlayerService {

    /** Creates a new service working with existing repository */
    def apply(repository: PlayerRepository, logging: Logging): PlayerService = new PlayerService {

      def deleteWorst(minimumScore: Int) = ???
      def celebrate(bonus: Int) = ???

    }

  }

}
// ScalaTest supports executing asynchronous code out of the box: just replace
// `AnyFunSuite` with `AsyncFunSuite` and make sure it returns `Future[Assertion]`:
// https://www.scalatest.org/user_guide/async_testing
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise12Spec
//
class Exercise12Spec extends AsyncFunSuite {

  import Exercise12._

  test("PlayerService.deleteWorst works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService(repository, logging)

    // perform the test
    service.deleteWorst(???) map { _ =>
      // validate the results
      assert(???)
    }
  }

  test("PlayerService.celebrate works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService(repository, logging)

    // perform the test
    service.celebrate(???) map { _ =>
      // validate the results
      assert(???)
    }

  }

}
// *Exercise 13*
//
// What are main problems of asynchronous testing? Try to name or guess them
// without reading further.
//
// One of the biggest is that we cannot really say if the test did not complete
// because of the bug or because it is still working.
//
// Modify Exercise 12 code so the test fails with a timeout by forgetting
// to complete the Promise (a bug), or just doing a long work (by calling
// `Thread.sleep(...)` in the code).
//
// Hint: never use `Thread.sleep` in your production code or test unless you
// are really sure what are you doing. We can discuss why later if we have
// the time.

// *Exercise 14*
//
// Warning: We did not introduce you to some of the
// concepts used below, but I really wanted you to show the trick, so you
// can have something to look forward to. It is fine if you do not understand
// what is happening below. Feel free to ask questions though.
//
// How can we avoid the issues of asynchronous execution? We can abstract
// over the way we execute!
//
object Exercise14 {

  // Remember what other structure allowed to do `map` and `flatMap`? It is
  // `Option`! What if we could replace `Future` with `Option` in our code
  // and make it synchronous magically.
  //
  // Remember Scala allows you to do pass type parameters to function in
  // classes like this?
  def function1[T](list: List[T]): Option[T] = list.headOption
  // And then call the function like this?
  val result: Option[Int] = function1(List(1, 2, 3))

  // We will use the same trick now and pass `Option` instead of `Future`
  // to the code.
  //
  // There is one catch. You have to use `T[_]` instead of `T` if you pass
  // a higher kinded type (see previous lecture).
  //
  case class Player(id: String, name: String, email: String, score: Int)
  trait PlayerRepository[F[_]] {
    def byId(id: String): F[Option[Player]]
    def all: F[List[Player]]
    def update(player: Player): F[Unit]
    def delete(id: String): F[Unit]
  }
  trait Logging[F[_]] {
    def info(message: String): F[Unit]
  }

  trait PlayerService[F[_]] {

    /** Deletes all the players with score lower than minimum.
      *
      * @param miniumumScore the minimum score the player stays with.
      */
    def deleteWorst(minimumScore: Int): F[Unit]

    /** Adds bonus points to score to all existing players
      *
      * @param bonus the bonus points to add to the players.
      */
    def celebrate(bonus: Int): F[Unit]

  }
  object PlayerService {

    // Note: we are using special `Monad` type from `cats` library to avoid some boilerplate.
    // It gives us `map` and `flatMap` methods on our `F`. I.e. it says:
    // "we have something that have `map` and `flatMap` on it".
    // You will learn more about `cats` library in next lecture.

    /** Creates a new service working with existing repository */
    def apply[F[_]: Monad](repository: PlayerRepository[F], logging: Logging[F]): PlayerService[F] = new PlayerService[F] {

      def deleteWorst(minimumScore: Int) = ???
      def celebrate(bonus: Int) = ???

    }

  }

}
// Now let's first copy-paste the code form Exercise 12 and replace all
// `Future` calls by `F` to prove it actually works.
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise14FutureSpec
//
class Exercise14FutureSpec extends AsyncFunSuite {

  import Exercise14._

  test("PlayerService.deleteWorst works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService[Future](repository, logging)

    // perform the test
    service.deleteWorst(???) map { _ =>
      // validate the results
      assert(???)
    }
  }

  test("PlayerService.celebrate works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService[Future](repository, logging)

    // perform the test
    service.celebrate(???) map { _ =>
      // validate the results
      assert(???)
    }

  }

}

// Now let's copy-paste the code form above and replace all
// `Future` calls by `Option` to enjoy rock stable tests.
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise14OptionSpec
//
// Bonus task: try to break the tests with `Thread.sleep(...)` call
// as above.
//
class Exercise14OptionSpec extends AnyFunSuite {

  import Exercise14._

  test("PlayerService.deleteWorst works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService[Option](repository, logging)

    // perform the test
    service.deleteWorst(???) map { _ =>
      // validate the results
      assert(???)
    }
  }

  test("PlayerService.celebrate works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService[Option](repository, logging)

    // perform the test
    service.celebrate(???) map { _ =>
      // validate the results
      assert(???)
    }

  }

}
//
// How can we be sure `Future` and `Option` behave the same way?
//
// We know they both have the same methods `map` and `flatMap`, i.e. they are Monads,
// but these could be doing anything, no?
//
// This is a valid question, and you cannot just rely on them behaving similar
// unless proven.
//
// The "behavior" of the similar structures is usually called a law. The laws
// for common structures such as `Monad` or `Traversable` are proven and commonly
// tested using property-based testing libraries such as ScalaCheck.
//
// These libraries allow checking that the specific property stands true not
// for a single parameter value, but for a whole range or domain.
//
// We will not do property testing this time, but you can read more about it here:
// https://www.scalatest.org/user_guide/property_based_testing


// What is the big problem with the code we wrote above? We use `var` and
// `AtomicReference` in our stubs a lot. It is a mutable state. Scala developers
// hate mutable state a lot.
//
// Besides that, we have to go inside of the stub and take data out of it sometimes,
// which might not always be convenient. Can we use our `Option` trick to somehow
// avoid it?
//
// And yes, we can, there is a special type in `cats` library mentioned above.
// It is called `State` and also have the `map` and `flatMap` methods we need.
// Plus it has "state" storage which we can use in our tests!
//
// Hint: `State` is rarely useful outside of testing. There are much better and
// more performant structures for that such as `Ref`. Do not go and refactor all
// your code to `State`.
//
// You can read more about it here:
// https://typelevel.org/cats/datatypes/state.html
//
// Let's use it to simplify our tests a bit.
//
// sbt:scala-bootcamp> testOnly *testing2.Exercise14StateSpec
//
// Bonus task: also store logging statements in `State`.
//
class Exercise14StateSpec extends AnyFunSuite {

  import Exercise14._
  type F[T] = State[List[Player], T]

  test("PlayerService.deleteWorst works correctly") {

    // construct fixture
    val repository = new PlayerRepository[F] {
      def byId(id: String) = State.pure(None)
      def all = State.get
      def update(player: Player) = State.modify { players =>
        players map { p =>
          if (p.id == player) player else p
        }
      }
      def delete(id: String) = State.modify { players =>
        players filterNot (_.id == id)
      }
    }
    val logging = ???
    val service = PlayerService[F](repository, logging)

    // perform the test
    service.deleteWorst(???) map { _ =>
      // validate the results
      assert(???)
    }
  }

  test("PlayerService.celebrate works correctly") {

    // construct fixture
    val repository = ???
    val logging = ???
    val service = PlayerService[F](repository, logging)

    // perform the test
    service.celebrate(???) map { _ =>
      // validate the results
      assert(???)
    }

  }

}
