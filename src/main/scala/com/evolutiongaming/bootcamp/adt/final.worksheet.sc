case class User(name: String)
class MyUser(name: String, lastName: String) extends User(name)

Set(
  new MyUser("John", "Doe"),
  new MyUser("John", "Doe2")
).size


new MyUser("John", "Doe") == new MyUser("John", "Doe2")

new MyUser("John", "Doe")


/*
     Make case classes final for equals, hashCode and toString to be correct.
     https://nrinaudo.github.io/scala-best-practices/tricky_behaviours/final_case_classes.html
*/
