package edu.luc.cs.cs371.echo
package main

object Main:
  def main(args: Array[String]): Unit =
    val echos = Seq(new impl.SimpleEcho, new impl.DoubleEcho)
    for e <- echos do
      println(e.echo("hello"))
end Main
