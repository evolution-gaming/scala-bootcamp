sealed trait Msg // can be extended in the same file only

case class CreateUser(name: String) extends Msg
case class RemoveUser(name: String) extends Msg
case object GetUsers extends Msg // object vs case object
