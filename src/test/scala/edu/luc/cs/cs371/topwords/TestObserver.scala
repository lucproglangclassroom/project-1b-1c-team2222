package edu.luc.cs.cs371.topwords

import org.scalatest.funsuite.AnyFunSuite

class TestObserver extends AnyFunSuite:

  test("OutputToBuffer should collect updates"):
    val obs = new OutputToBuffer:
      override type Result = String
    obs.update("hello")
    obs.update("world")
    assert(obs.buffer == Seq("hello", "world"))

  test("OutputToBuffer should start empty"):
    val obs = new OutputToBuffer:
      override type Result = Int
    assert(obs.buffer.isEmpty)

end TestObserver
