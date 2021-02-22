package com.evolutiongaming.bootcamp.typeclass

object HigherKindedTypes {

  /* 1. Higher Order Functions

  This lecture will contain a bit of theory to make it easier to understand how some more advanced type
  classes can be used in particular and how we can work with types in System Fω̲ type system (with types
  dependent on other types) in general.

  We'll start with refreshing of some functions theory we had previously.

  Currying – conversion of function with multiple arguments into a sequence of functions that each take just
  one argument.
  So function of 2 arguments can be converted into a function with just one first argument which returns other
  function which takes second argument and returns result.

  Example:
   */

  def multiArgFunction(a: String, b: Int): Long          = ??? // Type: `(String, Int) => Long`
  def curriedFunction(a: String)(b: Int): Long           = ??? // Type: `String => (Int => Long)`
  def equivalentCurriedFunction1(a: String): Int => Long = ???
  def equivalentCurriedFunction2: String => Int => Long  = ???

  /*
  All these 4 functions are equivalent and can be freely converted to each other. Even in reverse order.
  So if we have a function which returns other function (like `equivalentCurriedFunction2`) it can be
  converted to the function which doesn't return other function, but accepts more arguments
  (like `multiArgFunction`).
   */

  /*
  Exercise 1. "Uncurry" the following function to make it not return other functions,
  but accept more arguments.
   */
  def curriedFunction1: (Long => Boolean) => String => Int = ???

  /*
  Further in this "Higher Order Functions" block we assume that we are uncurrying all our functions so they
  don't return other functions.

  DEFINITION
  Now let's define order of function as a max order of arguments plus 1.
  Assume that order of "value" is 0 for completeness.

  Examples:
   */

  // Order 0 "function"
  def func0: String = ???

  // Typical First-Order Function
  def func1(a: String): Int = ???

  // Order 2 Function
  def func2(foo: Int => Boolean): Long = ??? // Type of `func2` is `(Int => Boolean) => Long`

  // Order 3 Function
  def func3(a: String, bar: (Int => Boolean) => Long): Long = ???

  // Example: Order 2 Function
  def func2a(a: String, bar: Int => (Boolean => Long)): Long = ???

  // Exercise 2. What is order of the following function?
  def funcX1(a: Int => String => Boolean): Long = ???

  // Exercise 3. What is order of the following function?
  def funcX2(a: String): (Long => Boolean) => String = ???

  /* Exercise 4. Write an example of Order 4 function:
  def func4(???): ??? = ???
   */

  /*
  Types as well as argument names in our signatures are not important to calculate order of function.
  Let's ignore them and see just pure "shape" of function (imagine that we are in non-typed world).

  So to describe "shape" or "kind" of the function we will replace values with stars `*` and functions with
  arrows `->`.

  ┌──────────┬───────────────────────────┐
  │ Function │          "Kind"           │
  ├──────────┼───────────────────────────┤
  │ func0    │ *                         │
  │ func1    │ * -> *                    │
  │ func2    │ (* -> *) -> *             │
  │ func3    │ * -> ((* -> *) -> *) -> * │
  └──────────┴───────────────────────────┘

  Step-by-Step (func3):
  - func3(a: String, bar: (Int => Boolean) => Long): Long
  - func3(a: String)(bar: (Int => Boolean) => Long): Long
  - (String)((Int => Boolean) => Long): Long
  - (String) => ((Int => Boolean) => Long) => Long
  - * -> ((* -> *) -> *) -> *
   */

  // Exercise 5. What are the kinds of `funcX1` and `funcX2`?
  // def funcX1(a: Int => String => Boolean): Long = ???
  // def funcX2(a: String): (Long => Boolean) => String = ???

  /* 2. Higher Kinded Types
  Let's switch from functions to types. Types also can have parameters – other types. For example: `List`,
  `Option`, `Map`.
  Such types which accepts other types are usually called type constructors. Also we can say that type
  `List[A]` depends on type `A`.
  Type constructor itself can't be used as a type for specific value. You can't do the following:

  val list: List = ???

  It will not compile.
  But type constructor can be "invoked" of "filled" with specific types and after that it can be used as a
  type for values.
   */

  // Example. `List` is a type constructor and it's "filled" with a type `Int`.
  val listOfInts: List[Int] = ???

  /*
  So we treat `List` here as a "function" which can accept other type `Int` as argument, and result of this
  "call" can be written as a `List[Int]` (which is a "value" in terms of functions).


  Now we can draw a parallel between types and functions.

  ┌────────────────────────────────────────────────┬───────────────────────────────┐
  │                     Types                      │           Functions           │
  ├────────────────────────────────────────────────┼───────────────────────────────┤
  │ Specific Type (String, Int, List[Int], etc.)   │ Value (Order 0 Function)      │
  │ Type Constructor (List, Option, Functor, etc.) │ Function (Order 1+ Functions) │
  │                                                │                               │
  │ Type Construction                              │ Evaluation                    │
  └────────────────────────────────────────────────┴───────────────────────────────┘
  Type Construction here is a process of Type Constructors application to define a type which can be used as a
  type for variable.


  Also you must have noticed that there are differences in Scala syntax we usually use for functions and for
  type constructors.
  Check following examples:

  ┌─────────────────────────────┬─────────────────────────────────────────────────────────────────────┐
  │            Type             │                              Function                               │
  ├─────────────────────────────┼─────────────────────────────────────────────────────────────────────┤
  │ Option[Content]             │ Content => OptionOfContent                                          │
  │ Map[Key, Value]             │ Key => Value => MapWithKeyAndValue                                  │
  │ Functor[Container[Content]] │ (Content => ContainerWithContent) => FunctorForContainerWithContent │
  └─────────────────────────────┴─────────────────────────────────────────────────────────────────────┘

  For type constructors we usually use underscore to represent type which is not important itself (so we
  don't need to have a name for it), but for which we need to know its kind.

  In the example above it's not important what we have in square brackets, so we can use underscores instead
  of `Content`, `Key`, `Value` and `Container`.
   */

  /* Exercise 6. Fill out orders of the following types:

  ┌───────────────────────┬───────┐
  │         Type          │ Order │
  ├───────────────────────┼───────┤
  │   Option[_]           │ ???   │
  │                       │       │
  │   Map[_, _]           │ ???   │
  │                       │       │
  │   trait Maybe[T]      │       │
  │   Maybe[_]            │ ???   │
  │                       │       │
  │   trait Functor[F[_]] │       │
  │   Functor[_[_]]       │ ???   │
  └───────────────────────┴───────┘
   */

  /* Exercise 7. Fill out missed kinds for the following types:

  ┌─────────────────────┬──────────────────┐
  │        Type         │       Kind       │
  ├─────────────────────┼──────────────────┤
  │ String              │ ???              │
  │ Option[_]           │ ???              │
  │ List[_]             │ ???              │
  │ Functor[_[_]]       │ ???              │
  │ Set[_]              │ ???              │
  │ Map[_, _]           │ ???              │
  │ Kleisli[_[_], _, _] │ ???              │
  └─────────────────────┴──────────────────┘
   */

  /* 3. The intuition behind HKT
  We already know that such types like `List[A]` allow us to abstract over their content. Like it's defined
  for any type `A`, so we can use it with `Int`, `String` or any other type we want.
  The same way functions allow us to abstract over values – their arguments. If we have a function which
  calculates `factorial` it works for any integer we pass into it. We don't have separate implementations
  for `3` or `7` or `42`.

  So in case of Higher Kinded Types or Higher Order Functions same logic continues to work. We abstract over
  types which already abstracts over other type. Or in case of functions we abstract over other functions.


  Now let's move to the practical part and see how all these can be used.

  Let's define type `Maybe` which will be our self-made version of `Option`.
   */

  sealed trait Maybe[+_] // it's equal to `Maybe[+A]`
  object Maybe {
    case object Empty extends Maybe[Nothing]
    case class Something[A](value: A) extends Maybe[A]

    val empty: Maybe[Nothing]            = Empty
    def something[A](value: A): Maybe[A] = Something(value)
  }

  // `Maybe` is defined for any type `A` as we don't use any specifics of A. We abstract over this type.

  // Exercise 8. Implement `Disjunction` – your own version of `Either`
  sealed trait Disjunction[???]
  object Disjunction {
    // ???
  }

  /*
  But what if we want to abstract over all types for which we can call `map`? We have `Option`, `List`,
  `Vector`, `Try`, different types of collections with such method. And looks like all of them already
  abstracts over their content.

  So we'll have Order 2 type is this case.
  Let's check an example of the type class we can define for that.
   */

  // 1. Typeclass interface
  trait Functor[F[_]] {
    def fmap[A, B](fa: F[A])(f: A => B): F[B]
  }

  // 2. Syntax helper
  implicit class FunctorOps[F[_], A](fa: F[A])(implicit functor: Functor[F]) {
    def fmap[B](f: A => B): F[B] = implicitly[Functor[F]].fmap(fa)(f)
  }

  /*
  Also in Scala there is a syntactic sugar for:

  def/class SomeName[F[_]](...)(implicit a: TypeClass[F])

  it's equal to:

  def/class SomeName[F[_]: TypeClass](...)

  So `implicit class FunctorOps[F[_]: Functor, A](fa: F[A])` is also possible definition.
   */

  // 3.   Type class instances
  // 3.1. For `Maybe`
  implicit val maybeFunctor: Functor[Maybe] = new Functor[Maybe] {
    override def fmap[A, B](fa: Maybe[A])(f: A => B): Maybe[B] = fa match {
      case Maybe.Empty              => Maybe.Empty
      case Maybe.Something(content) => Maybe.Something(f(content))
    }
  }

  // 3.2. For `List`
  // Exercise 9. Implement for `List`
  implicit val listFunctor: Functor[List] = new Functor[List] {
    override def fmap[A, B](fa: List[A])(f: A => B): List[B] = ???
  }

  // So now we can use `map` for any type `F` for which we have `Functor[F]` implemented.

  Maybe.something(1).fmap(_ + 1)

  List(1, 2, 3).fmap(_ + 2)

  /*
  What if we want to implement `Functor` for values of `Map`? Map accepts 2 types – Key and Value,
  but `F` in `Functor[F[_]]` definition accepts just one type.

  What can we do with that? How do we handle such situation in case of functions?
   */

  def funcLikeFunctor(f: Any => Any): Any    = ??? // (* -> *) -> *
  def funcLikeMap(key: Any, value: Any): Any = ??? // * -> * -> *

  /*
  Exercise 10. How can we pass `funcLikeMap` into `funcLikeFunctor` so code will compile?
  Tip: We can have its own "functor" for each needed "key".
   */

  // funcLikeFunctor(??? funcLikeMap ???)

  /*
  So we've converted kind (* -> * -> *) to kind (* -> *).

  Same way we can do that for `Functor` and `Map`.
   */

  type MapWithStringKeys[A] = Map[String, A]

  implicit val mapFunctor: Functor[MapWithStringKeys] = new Functor[MapWithStringKeys] {
    override def fmap[A, B](fa: MapWithStringKeys[A])(f: A => B): MapWithStringKeys[B] = ???
  }

  // How can we do that without introducing explicit type name in the context?

  implicit val mapFunctor1: Functor[({ type MapWithStringKeys1[A] = Map[String, A] })#MapWithStringKeys1] = ???

  implicit val mapFunctor2: Functor[({ type λ[A] = Map[String, A] })#λ] = ???

  implicit val mapFunctor3: Functor[Map[String, *]] = ???

  // Exercise 11. Implement Functor for `Map`.
  implicit def mapFunctor4[T]: Functor[Map[T, *]] = ???

  // Exercise 12. Implement Functor for `Disjunction`
  // implicit val disjunctionFunctor = ???
}
