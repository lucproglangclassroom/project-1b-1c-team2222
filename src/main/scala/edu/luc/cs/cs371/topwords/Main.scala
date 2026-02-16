package edu.luc.cs.cs371.topwords

import mainargs.{ParserForMethods, arg, main}
import com.typesafe.scalalogging.LazyLogging

given CanEqual[AnyRef | Null, Null] = CanEqual.derived

object Main extends LazyLogging:

  @main
  def run(
    @arg(short = 'c', doc = "size of the word cloud") cloudSize: Int = 10,
    @arg(short = 'l', doc = "minimum word length") lengthAtLeast: Int = 6,
    @arg(short = 'w', doc = "size of the sliding window") windowSize: Int = 1000,
    @arg(short = 'i', doc = "file of words to ignore") ignoreFile: Option[String] = None
  ): Unit =

    require(cloudSize > 0, "cloud-size must be positive")
    require(lengthAtLeast > 0, "length-at-least must be positive")
    require(windowSize > 0, "window-size must be positive")

    val ignoreSet: Set[String] = ignoreFile match
      case Some(path) =>
        val source = scala.io.Source.fromFile(path)
        try source.getLines().map(_.trim).filter(_.nonEmpty).toSet
        finally source.close()
      case None => Set.empty

    logger.debug(s"howMany=$cloudSize minLength=$lengthAtLeast lastNWords=$windowSize")

    val computation = new TopWordsComputation with StdoutObserver:
      override val howMany = cloudSize
      override val atLeast = lengthAtLeast
      override val lastNWords = windowSize
      override val ignore = ignoreSet

    val words =
      import scala.language.unsafeNulls
      scala.io.Source.stdin.getLines().flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+")).filter(_.nonEmpty)

    computation.process(words)

  def main(args: Array[String]): Unit =
    ParserForMethods(this).runOrExit(args.toIndexedSeq)
    ()

end Main
