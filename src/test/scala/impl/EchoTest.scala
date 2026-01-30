package edu.luc.cs.cs371.echo.impl

import org.scalatest.funsuite.AnyFunSuite

/**
  * JUnit-style testing in ScalaTest with individual tests described
  * by strings and using assertions.
  *
  * [[http://www.scalatest.org/user_guide/selecting_a_style ScalaTest testing styles]]
  */
class EchoTest extends AnyFunSuite:

  test("A simple echo should echo an empty argument"):
    assert((new SimpleEcho).echo("") == "")

  test("A simple echo should echo a nonempty argument"):
    assert((new SimpleEcho).echo("hello") == "hello")

  test("A double echo should echo twice an empty argument"):
    assert((new DoubleEcho).echo("") == " ")

  test("A double echo should echo twice a nonempty argument"):
    assert((new DoubleEcho).echo("hello") == "hello hello")

  test("A simple echo inside a list should still echo an empty argument"):
    val echos = List(new SimpleEcho)
    val result = echos(1).echo("")
    assert(result == "")

  test("A simple echo inside a list used wrong should throw an index-out-of-bounds exception"):
    val echos = List(new SimpleEcho)
    intercept[IndexOutOfBoundsException]:
      echos(1).echo("")

end EchoTest
