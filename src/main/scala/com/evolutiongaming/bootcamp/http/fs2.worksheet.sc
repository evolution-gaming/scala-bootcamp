import fs2.{Pipe, Pull, Pure, Stream}
import cats.effect.IO
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

implicit val timer = IO.timer(ExecutionContext.global)

// fs2.Stream[F[_], O] emits 0..∞ values of type O in the effect F

// Finite streams
// You can convert them to lists just calling `.toList` method.

Stream.emit(1)
Stream.emits(List(1, 2, 3))
Stream(1, 2, 3)

// Infinite streams:
// Make sure to call `take` method (or similar) before converting them to lists.

Stream.constant(42)
Stream.iterate(1)(_ + 2)
Stream.unfold(1)(s => Some((s, s + 2)))

// Effectful streams:
// To convert them to list you have to `compile` them first, i.e. compose all effects into one.

Stream.eval(IO(println("hello")))
Stream.random[IO].take(10).compile
Stream.awakeEvery[IO](1.second)


// fs2.Pipe[F[_], I, O] is a type synonym for `Stream[F, I] => Stream[F, O]`

val pipe: Pipe[Pure, Int, Int] = _.map(_ + 1)
Stream(1, 2, 3).through(pipe)


// fs2.Pull[F[_], O, R] pulls values from a stream, writes output of type O, and returns a result of type R.

Stream(1, 2, 3, 4, 5).pull.uncons1.flatMap {
  case Some(head -> tail) => tail.map(_ + head).pull.echo
  case None => Pull.done
}.stream
