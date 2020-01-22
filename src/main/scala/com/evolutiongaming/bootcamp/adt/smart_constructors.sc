final case class User private (name: String, age: Int)

object User {
  def create(name: String, age: Int): Either[String, User] = {
    if (age >= 0) {
      Right(User(name, age))
    } else {
      Left("Age can't be negative")
    }
  }
}

User("John", -5) // only `new User("", -1)` is private
User.create("John", 25).map(_.copy(age = -5))

sealed abstract case class User(name: String, age: Int)

object User {
  def create(name: String, age: Int): Either[String, User] = {
    if (age >= 0) {
      Right(new User(name, age) {})
    } else {
      Left("Age can't be negative")
    }
  }
}

/*
    can't do User("John", -5)
    no copy method

    https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2
*/
