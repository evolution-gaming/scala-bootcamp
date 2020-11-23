import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object Cervantes extends IOApp {

  def run(args: List[String]): IO[ExitCode] = IO {
    println("""
      “How canst thou say that!” answered Don Quixote; “dost thou not hear the
      neighing of the steeds, the braying of the trumpets, the roll of the
      drums?”

      Miguel de Cervantes
    """)
  } as ExitCode.Success

}
