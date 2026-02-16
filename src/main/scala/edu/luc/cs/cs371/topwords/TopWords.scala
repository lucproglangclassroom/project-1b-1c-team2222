package edu.luc.cs.cs371.topwords

import scala.collection.mutable

/** Core computation for the streaming word cloud. Depends on an Observer mixin. */
trait TopWordsComputation:
  self: Observer =>
  override type Result = Seq[(String, Int)]

  val howMany: Int
  val atLeast: Int
  val lastNWords: Int
  val ignore: Set[String]

  def process(words: Iterator[String]): Unit =
    val window = mutable.Queue.empty[String]
    val freq = mutable.Map.empty[String, Int]

    for word <- words do
      if word.length >= atLeast && !ignore.contains(word) then
        // Add new word to window and update frequency
        window.enqueue(word)
        freq(word) = freq.getOrElse(word, 0) + 1

        if window.size > lastNWords then
          // Evict oldest word
          val oldest = window.dequeue()
          val oldCount = freq(oldest)
          if oldCount == 1 then
            freq.remove(oldest): Unit
          else
            freq(oldest) = oldCount - 1

        if window.size >= lastNWords then
          // Produce word cloud: top howMany words by frequency descending
          val cloud = freq.toSeq.sortBy(-_(1)).take(howMany)
          update(cloud)

end TopWordsComputation
