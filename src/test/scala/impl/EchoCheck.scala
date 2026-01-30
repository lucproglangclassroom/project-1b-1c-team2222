package edu.luc.cs.cs371.echo.impl

import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.Checkers

/**
  * Property-based testing in ScalaTest using the Checkers mixin
  * (preferred over using ScalaCheck directly).
  *
  * [[http://www.scalatest.org/user_guide/writing_scalacheck_style_properties Using ScalaTest with Checkers]]
  */
class EchoCheck extends AnyFunSuite with Checkers:

  val simpleEcho = new SimpleEcho

  val doubleEcho = new DoubleEcho

  test("A simple echo should echo its argument"):
    check:
      (a: String) =>
        simpleEcho.echo(a) == a

  test("A double echo should echo twice its argument"):
    check:
      (a: String) =>
        doubleEcho.echo(a) == a + " " + a

end EchoCheck
