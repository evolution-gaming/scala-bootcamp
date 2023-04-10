// Monad

import cats.Monad

// Monad is a type with "flatMap" and "pure" satisfying certain laws
// Laws:
// Associativity: fa.flatMap(f).flatMap(g) <-> fa.flatMap(a => f(a).flatMap(g))
// Left identity: pure(a).flatMap(f) <-> f(a)
// Right identity: fa.flatMap(pure) <-> fa

// Sample implementation for Option
object OptionMonad extends Monad[Option] {
  override def pure[A](x: A) = Some(x)

  override def flatMap[A, B](fa: Option[A])(f: A => Option[B]) = fa match {
    case Some(a) => f(a)
    case None    => None
  }

  // This is a special method for stack-safety, we'll get to it later
  @scala.annotation.tailrec
  override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] = {
    f(a) match {
      case Some(Left(a))  => tailRecM(a)(f)
      case Some(Right(b)) => Some(b)
      case None           => None
    }
  }
}

// There's FlatMap class with just flatMap without pure, most syntax operations are there

import cats.syntax.flatMap._
// Functor syntax will be needed for for-comprehensions
import cats.syntax.functor._

// In more general sense, monad is about sequencing computations
// E.g with "a <- fa" in for-comprehension, remainder cannot proceed without an "a"

trait User
trait GiftCard
trait Gift

def findUser(name: String): Option[User]       = ???
def findGiftCard(user: User): Option[GiftCard] = ???
def pickGift(card: GiftCard): Option[Gift]     = ???

// You can just flatMap those together
def gift1(name: String): Option[Gift] =
  findUser(name).flatMap(findGiftCard).flatMap(pickGift)

// Or you can use for-comprehensions
def gift2(name: String): Option[Gift] = {
  for {
    user     <- findUser(name)
    giftCard <- findGiftCard(user)
    gift     <- pickGift(giftCard)
  } yield gift
}

// On associativity: gift2 desugars to something a bit different that gift1
def gift2Alt(name: String): Option[Gift] = {
  findUser(name).flatMap(user => findGiftCard(user).flatMap(giftCard => pickGift(giftCard)))
}
// Associativity: fa.flatMap(f).flatMap(g) <-> fa.flatMap(a => f(a).flatMap(g))
// This law requires those two to be equivalent

// Monad is also an Applicative, and Applicative's behavior must be consistent with Monad
// https://impurepics.com/posts/2019-03-18-monad-applicative-consistency-law.html

// This forbids some behaviors, e.g. error accumulation in Validated:
case class User(name: String, age: Int)
val bob   = User("Bob", 32)
val alice = User("Alice", 23)

def minAge[F[_]: Monad](fa: F[User], fb: F[User]): F[Int] = {
  for {
    a <- fa
    b <- fb // Computation cannot proceed here without an A
  } yield a.age.min(b.age)
}

import cats.syntax.either._

minAge(bob.rightNel[String], alice.rightNel[String])
minAge(bob.rightNel[String], "ERROR".leftNel[User])
minAge("ERROR".leftNel[User], "ALSO ERROR".leftNel[User])

// Other operations

// There's >>, behaves like *>, but takes right argument by-name
// https://impurepics.com/posts/2019-02-09-operator-wars-reality.html

import cats.effect.IO

val ioOne = IO(println("Computing")) >> IO(1)
ioOne.unsafeRunSync()

// Bonus topic: stack-safety
// You can flatMap recursively, useful for IO

// Execute something repeatedly
val spamOnce              = IO(println("SPAM"))
def spamForever: IO[Unit] = spamOnce.flatMap(_ => spamForever)
val spamForeverValue      = spamForever

// There's a combinator for that
spamOnce.foreverM

// Practical example, execute something every second

import cats.effect.Timer

import scala.concurrent.duration._

def repeatEachSecond(action: IO[Unit])(implicit timer: Timer[IO]): IO[Unit] = {
  (IO.sleep(1.second) *> action).foreverM
}

// Those operations are technically stack-safe, but not valid for @tailrec
// That's the reason for tailRecM
