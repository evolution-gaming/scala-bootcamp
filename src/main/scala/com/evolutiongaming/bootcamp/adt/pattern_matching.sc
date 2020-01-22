final case class User(name: String, age: Int)

val John2 = "John2"

val userf: User => String = {
  case User("John", age)            => s"John $age" // use age, string interpolation
  case User(`John2`, age)           => s"second John $age" // match on constant
  case User("John3" | "John4", age) => s"maybe three or four John $age" // |
  case User(name, age) if age < 20  => s"smol $name" // if
  case _                            => "anyone"
}

userf(User("John", 25))
userf(User("John2", 25))
userf(User("John4", 25))
userf(User("John5", 15))


val myRegex = raw"2,(\d),(\d)".r

val stringf: String => (String, String) = {
  case s"1,${x},${y}" => (x, y)
  case myRegex(x, y)  => (x, y)
  case _              => ("", "")
}

stringf("1,2,3")
stringf("2,3,4")
stringf("lalala")


sealed trait Light

case object Red extends Light
case object Yellow extends Light
case object Green extends Light

val lightf: Light => Unit = {
  case Red => ()
  case Green => ()
}

//    Warning:(37, 29) match may not be exhaustive.
//    It would fail on the following input: Yellow
//    val lightf: Light => Unit = {
