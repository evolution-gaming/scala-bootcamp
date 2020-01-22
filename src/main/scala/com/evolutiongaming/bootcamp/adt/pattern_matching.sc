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

val stringPf: String => (String, String) = {
  case s"1,${x},${y}" => (x, y)
  case myRegex(x, y)  => (x, y)
  case _              => ("", "")
}

stringPf("1,2,3")
stringPf("2,3,4")
stringPf("lalala")
