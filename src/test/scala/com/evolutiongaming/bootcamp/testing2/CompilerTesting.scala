package com.evolutiongaming.bootcamp.testing2

// *Introduction*
//
// The bug which is not respresentable using the code will never happen.
// The simplest example of such defense is introduction of type system. I.e.
// if you have marked your field as `Integer` in your Java app, we do not need
// to test if it contains `String` inside. It simply cannot.
//
// Scala is much more powerful in that area, i.e., for example, we can make
// compile check if number is positive, if string is an actual e-mail etc. which
// is called refined types (https://github.com/fthomas/refined). We can also
// make sure the part of the code never accesses the database unless asked to do
// so etc., which is called effect tracking (https://typelevel.org/cats-effect/).
//
// It is not just a cool rocket science tech, we are using this stuff every day,
// and most Scala developers here won't be surprised if you ask them about it.
//
object CompilerTesting