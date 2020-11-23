import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object Knuth extends IOApp {

  def run(args: List[String]): IO[ExitCode] = IO {
    println("""
      Premature optimization is the root of all evil

      Donald Knuth
    """)
  } as ExitCode.Success

}
