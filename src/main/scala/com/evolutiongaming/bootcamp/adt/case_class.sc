case class User(name: String, age: Int)

val user = User("John", 30) // constructor without `new`

user.name // fields are public

// user.name = "Not John" won't compile

user // pretty toString

user == User("John", 30) // equals is here

User("John", 30).hashCode() // hashCode is here
user.hashCode()
