package edu.luc.cs.cs371.topwords

import scala.collection.immutable.Queue

/** Immutable state for the sliding window computation. */
case class WindowState(
  window: Queue[String],
  freq: Map[String, Int]
) derives CanEqual

/** Core computation trait: purely functional sliding window word cloud.
  * Returns an Iterator of word clouds — no side effects.
  */
trait TopWordsComputation:
  val howMany: Int
  val atLeast: Int
  val lastNWords: Int
  val ignore: Set[String]

  /** Pure function: takes an iterator of words, returns an iterator of word clouds. */
  def process(words: Iterator[String]): Iterator[Seq[(String, Int)]] =
    words
      .filter(w => w.length >= atLeast && !ignore.contains(w))
      .scanLeft(WindowState(Queue.empty, Map.empty)): (state, word) =>
        val newWindow = state.window.enqueue(word)
        val newFreq = state.freq.updated(word, state.freq.getOrElse(word, 0) + 1)
        if newWindow.size > lastNWords then
          val (oldest, trimmedWindow) = newWindow.dequeue
          val oldCount = newFreq(oldest)
          val trimmedFreq =
            if oldCount == 1 then newFreq.removed(oldest)
            else newFreq.updated(oldest, oldCount - 1)
          WindowState(trimmedWindow, trimmedFreq)
        else
          WindowState(newWindow, newFreq)
      .drop(1)
      .filter(_.window.size >= lastNWords)
      .map(state => state.freq.toSeq.sortBy(-_(1)).take(howMany))

end TopWordsComputation

/** Format a word cloud as a space-separated string of "word: count" pairs. */
def formatCloud(cloud: Seq[(String, Int)]): String =
  cloud.map((w, f) => s"$w: $f").mkString(" ")

/** Consume an iterator of word clouds, printing each to stdout.
  * Stops on SIGPIPE (when downstream pipe closes).
  */
def runWithStdIO(results: Iterator[Seq[(String, Int)]]): Unit =
  results
    .map(formatCloud)
    .takeWhile: _ =>
      !System.out.checkError()
    .foreach(println)
