package edu.luc.cs.cs371.topwords

import scala.collection.mutable.Buffer

/** Observer pattern: receives computation results. */
trait Observer:
  type Result
  def update(result: Result): Unit
end Observer

/** Observer that prints formatted word cloud to stdout with SIGPIPE handling. */
trait StdoutObserver extends Observer:
  override type Result = Seq[(String, Int)]
  override def update(result: Result): Unit =
    val line = result.map((w, f) => s"$w: $f").mkString(" ")
    println(line)
    if System.out.checkError() then
      sys.exit(1)
end StdoutObserver

/** Observer that collects results in a buffer for testing. */
trait OutputToBuffer extends Observer:
  val buffer: Buffer[Result] = Buffer.empty[Result]
  override def update(result: Result): Unit = buffer.append(result)
end OutputToBuffer
