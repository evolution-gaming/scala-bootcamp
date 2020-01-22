Boolean // how many?


sealed trait Light // how many?

case object Red extends Light
case object Yellow extends Light
case object Green extends Light

case class TrafficLight(light: Light, isBlinking: Boolean) // how many?


sealed trait UnstableTrafficLight // how many?

case class TrafficLightOn(light: Light, isBlinking: Boolean) extends UnstableTrafficLight
case object TrafficLightOff extends UnstableTrafficLight


type X = (Boolean, Boolean)
type X = (Boolean, Light)

type X = Either[Boolean, Boolean]
type X = Either[Boolean, Light]

type X = (Boolean, Boolean, Light)


type X = Unit
type X = Nothing


type T

type X = Either[Unit, T]
type X = Either[Nothing, T]
type X = (Unit, T)
type X = (Nothing, T)

def absurd[A]: Nothing => A
