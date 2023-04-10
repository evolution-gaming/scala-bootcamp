import scala.collection.immutable.VectorImpl
import doobie.util.transactor
// ╔═══════════════════════════════════════╗
// ║ H i g h e r   K i n d e d   T y p e s ║
// ╚═══════════════════════════════════════╝

def someFunction[F[_]](
  ints: F[Int],
  strings: F[String],
): F[(Int, String)] = null.asInstanceOf[F[(Int, String)]]

someFunction(Vector(1), Vector("asd"))
someFunction(List(1), List("asd"))
// someFunction(Vector(1), List("asd"))

trait Mapper[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Zipper[F[_]] {
  def zip[A, B](fa: F[A], fb: F[B]): F[(A, B)]
}

trait Single[F[_]] {
  def single[A](a: A): F[A]
}

//-------- Vector --------
val vecMapper = new Mapper[Vector] {
  def map[A, B](fa: Vector[A])(f: A => B): Vector[B] = fa.map(f)
}

val vecZipper = new Zipper[Vector] {
  def zip[A, B](
    fa: Vector[A],
    fb: Vector[B],
  ): Vector[(A, B)] = fa.zip(fb)
}

val vecSingle = new Single[Vector] {
  def single[A](a: A): Vector[A] = Vector(a)
}

//-------- Option --------

val optionMapper = new Mapper[Option] {
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
}

val optionZipper = new Zipper[Option] {
  def zip[A, B](
    fa: Option[A],
    fb: Option[B],
  ): Option[(A, B)] = fa.zip(fb)
}

val optionSingle = new Single[Option] {
  def single[A](a: A): Option[A] = Some(a)
}

// --------- Result ---------
type Result[+A] = Either[String, A]
// val resultMapper = new Mapper[Either[String, *]]] {
// scala 3 : new Mapper[[a] =>> Either[String, a]] {
val resultMapper = new Mapper[Result] {
  def map[A, B](fa: Either[String, A])(f: A => B): Either[String, B] = fa.map(f)
}

// val resultZipper = new Zipper[Either[String, *]] {
// scala 3 : new Zipper[[a] =>> Either[String, a]] {
val resultZipper = new Zipper[Result] {
  def zip[A, B](
    fa: Either[String, A],
    fb: Either[String, B],
  ): Either[String, (A, B)] =
    for (a <- fa; b <- fb) yield (a, b)
}

val resultSingle = new Single[Result] {
  def single[A](a: A): Either[String, A] = Right(a)
}

// --------- Either[E, *] ---------
def eitherMapper[E] = new Mapper[Either[E, *]] {
  def map[B, C](
    fa: Either[E, B]
  )(f: B => C): Either[E, C] = fa.map(f)
}

def eitherZipper[E] = new Zipper[Either[E, *]] {
  def zip[B, C](
    fa: Either[E, B],
    fb: Either[E, C],
  ): Either[E, (B, C)] =
    for (a <- fa; b <- fb) yield (a, b)
}

def eitherSingle[E] = new Single[Either[E, *]] {
  def single[B](b: B): Either[E, B] = Right(b)
}

// --------- Traverse ----------

def collect[F[_], A](
  as: Vector[F[A]]
)(mapper: Mapper[F], zipper: Zipper[F], single: Single[F]): F[Vector[A]] =
  as.foldLeft(single.single(Vector.empty[A])) { (acc: F[Vector[A]], fa: F[A]) =>
    val zipped: F[(Vector[A], A)] = zipper.zip(acc, fa)
    mapper.map(zipped) { case (acc, a) => acc :+ a }
  }

collect[Result, String](Vector(Right("a"), Right("b"), Right("c")))(
  resultMapper,
  resultZipper,
  resultSingle,
)

// ------- More Kinds ---------
def hkt1[
  K3[_, _, _, _],
  K4[a[b[c]]],
  K1[+_],
  K2[-_],
  K5[_ >: String <: AnyRef],
  K6[+F[x] <: Seq[x], +G[x] <: Seq[_]],
  K7[+A[x, +y <: String, z >: Int] <: Either[x, y]],
  K8[A <: B, B],
  K9[X[x] <: Y[x], Y[+x]],
] = ()
