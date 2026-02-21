package edu.luc.cs.cs371.topwords

import org.scalatest.funsuite.AnyFunSuite

class TestTopWords extends AnyFunSuite:

  def createSUT(cloudSize: Int = 10, minLength: Int = 6, windowSize: Int = 1000, ignoreSet: Set[String] = Set.empty) =
    new TopWordsComputation:
      override val howMany: Int = cloudSize
      override val atLeast: Int = minLength
      override val lastNWords: Int = windowSize
      override val ignore: Set[String] = ignoreSet

  test("empty input produces no output"):
    val sut = createSUT()
    val result = sut.process(Iterator.empty).toList
    assert(result.isEmpty)

  test("input shorter than window produces no output"):
    val sut = createSUT(windowSize = 5, minLength = 1)
    val result = sut.process(Iterator("hello", "world", "foo", "bar")).toList
    assert(result.isEmpty)

  test("output starts when window is full"):
    val sut = createSUT(windowSize = 3, minLength = 1)
    val result = sut.process(Iterator("aaa", "bbb", "ccc")).toList
    assert(result.size == 1)

  test("words below minLength are ignored"):
    val sut = createSUT(windowSize = 3, minLength = 4)
    val result = sut.process(Iterator("ab", "cd", "abcd", "efgh", "ijkl")).toList
    assert(result.size == 1)
    val cloud = result.head
    assert(cloud.map(_(0)).toSet == Set("abcd", "efgh", "ijkl"))

  test("enough qualifying words produce output with short words mixed in"):
    val sut = createSUT(windowSize = 2, minLength = 3)
    val result = sut.process(Iterator("ab", "abc", "a", "def")).toList
    assert(result.size == 1)
    val cloud = result.head
    assert(cloud.map(_(0)).toSet == Set("abc", "def"))

  test("sliding window evicts oldest word"):
    val sut = createSUT(windowSize = 2, minLength = 1, cloudSize = 10)
    val result = sut.process(Iterator("aaa", "bbb", "ccc")).toList
    assert(result.size == 2)
    val lastCloud = result.last
    val words = lastCloud.map(_(0)).toSet
    assert(words == Set("bbb", "ccc"))
    assert(!words.contains("aaa"))

  test("frequency updates correctly with repeated words"):
    val sut = createSUT(windowSize = 3, minLength = 1, cloudSize = 10)
    val result = sut.process(Iterator("aaa", "aaa", "aaa")).toList
    assert(result.size == 1)
    val cloud = result.head
    assert(cloud == Seq(("aaa", 3)))

  test("cloud size limits output to howMany entries"):
    val sut = createSUT(windowSize = 5, minLength = 1, cloudSize = 2)
    val result = sut.process(Iterator("aaa", "bbb", "ccc", "ddd", "eee")).toList
    assert(result.size == 1)
    val cloud = result.head
    assert(cloud.size == 2)

  test("cloud size larger than distinct words shows fewer entries"):
    val sut = createSUT(windowSize = 3, minLength = 1, cloudSize = 10)
    val result = sut.process(Iterator("aaa", "bbb", "ccc")).toList
    assert(result.size == 1)
    val cloud = result.head
    assert(cloud.size == 3)

  test("window size 1 produces one cloud per qualifying word"):
    val sut = createSUT(windowSize = 1, minLength = 1, cloudSize = 10)
    val result = sut.process(Iterator("aaa", "bbb", "ccc")).toList
    assert(result.size == 3)
    assert(result(0) == Seq(("aaa", 1)))
    assert(result(1) == Seq(("bbb", 1)))
    assert(result(2) == Seq(("ccc", 1)))

  test("ignore list filters words"):
    val sut = createSUT(windowSize = 2, minLength = 1, ignoreSet = Set("bbb"))
    val result = sut.process(Iterator("aaa", "bbb", "ccc")).toList
    assert(result.size == 1)
    val cloud = result.head
    val words = cloud.map(_(0)).toSet
    assert(!words.contains("bbb"))
    assert(words == Set("aaa", "ccc"))

  test("full assignment example: -c 3 -l 2 -w 5"):
    val sut = createSUT(cloudSize = 3, minLength = 2, windowSize = 5)
    val input = "a b c aa bb cc aa bb aa bb a b c aa aa aa".split(" ")
    val result = sut.process(input.iterator).toList

    assert(result.size == 6)

    val first = result.head
    assert(first.size == 3)
    val firstMap = first.toMap
    assert(firstMap("aa") == 2)
    assert(firstMap("bb") == 2)
    assert(firstMap("cc") == 1)

    val last = result.last
    assert(last.size == 2)
    assert(last.head == ("aa", 4))
    assert(last(1) == ("bb", 1))

  // --- Interactive behavior tests ---
  // Verify that output is produced lazily (one result per qualifying input, not batched)

  test("process produces output lazily - each qualifying word triggers immediate result"):
    val sut = createSUT(windowSize = 2, minLength = 1, cloudSize = 10)
    val inputWords = List("aaa", "bbb", "ccc", "ddd")
    val resultIterator = sut.process(inputWords.iterator)

    // After the window fills (2 words), each subsequent word should produce one more result.
    // Total qualifying: 4 words, window=2, so 3 outputs.
    // We pull results one at a time to verify laziness.
    assert(resultIterator.hasNext)
    val first = resultIterator.next()
    assert(first.map(_(0)).toSet == Set("aaa", "bbb"))

    assert(resultIterator.hasNext)
    val second = resultIterator.next()
    assert(second.map(_(0)).toSet == Set("bbb", "ccc"))

    assert(resultIterator.hasNext)
    val third = resultIterator.next()
    assert(third.map(_(0)).toSet == Set("ccc", "ddd"))

    assert(!resultIterator.hasNext)

  test("process with tracing iterator confirms input-output interleaving"):
    // Use a custom iterator that records when elements are pulled
    var pullCount = 0
    val tracingIterator = new Iterator[String]:
      private val underlying = List("aaa", "bbb", "ccc", "ddd").iterator
      def hasNext: Boolean = underlying.hasNext
      def next(): String =
        pullCount += 1
        underlying.next()

    val sut = createSUT(windowSize = 2, minLength = 1, cloudSize = 10)
    val resultIterator = sut.process(tracingIterator)

    // Pull first result
    val r1 = resultIterator.next()
    val pullsAfterFirst = pullCount
    // At least 2 words must have been pulled (to fill window)
    assert(pullsAfterFirst >= 2)

    // Pull second result - should pull exactly one more word
    val r2 = resultIterator.next()
    val pullsAfterSecond = pullCount
    assert(pullsAfterSecond == pullsAfterFirst + 1)

    // Pull third result - should pull exactly one more word
    val r3 = resultIterator.next()
    val pullsAfterThird = pullCount
    assert(pullsAfterThird == pullsAfterSecond + 1)

  test("formatCloud formats correctly"):
    assert(formatCloud(Seq(("hello", 5))) == "hello: 5")
    assert(formatCloud(Seq(("hello", 5), ("world", 3))) == "hello: 5 world: 3")
    assert(formatCloud(Seq.empty) == "")

end TestTopWords
