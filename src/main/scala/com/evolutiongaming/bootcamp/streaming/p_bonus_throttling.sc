import cats.effect.concurrent.MVar
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.chaining.scalaUtilChainingOps

implicit val scheduler: Scheduler = Scheduler.apply(ExecutionContext.parasitic)

// Example: Emit one element per interval, drop the rest
def throttleSample[T](interval: FiniteDuration)(in: Observable[T]): Observable[T] = {
  Observable.from(MVar.empty[Task, Unit]).flatMap { loopBack =>
    val resets = Observable.repeatEvalF(loopBack.take).delayOnNext(interval)

    in.publishSelector { hot =>
      Observable(
        hot.map(Right(_)),
        resets
          .map(Left(_))
          .takeUntil(hot.completed), // ensure end stream completes when input completes
      ).merge
    }.mapAccumulate(true) {
      case (true, Right(element)) => false -> Some(element)
      case (false, Right(_))      => false -> None
      case (_, Left(_))           => true  -> None
    }.collect { case Some(value) => value }
      .doOnNext(_ => loopBack.tryPut(()).void)
  }
}

val ints = Observable.from(1 to 10)

ints
  .delayOnNext(100.millis)
  .pipe(throttleSample(250.millis))
  .dump("throttle-manual")
  .completedL
  .runSyncUnsafe(10.seconds)

// Also available in monix
ints
  .delayOnNext(100.millis)
  .throttleFirst(250.millis)
  .dump("throttle-monix")
  .completedL
  .runSyncUnsafe()

// Example: buffer which collects items and emits once per interval
def bufferTimed[T](interval: FiniteDuration)(in: Observable[T]): Observable[Seq[T]] = {
  in.publishSelector { hot =>
    val ticks = Observable.intervalWithFixedDelay(interval).takeUntil(hot.completed)
    hot.bufferWithSelector(ticks)
  }
}
