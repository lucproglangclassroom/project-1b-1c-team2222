package edu.luc.cs.cs371.topwords

import org.scalatest.funsuite.AnyFunSuite

class TestTopWords extends AnyFunSuite:

  import scala.language.unsafeNulls

  def createSUT(cloudSize: Int = 10, minLength: Int = 6, windowSize: Int = 1000, ignoreSet: Set[String] = Set.empty) =
    new TopWordsComputation with OutputToBuffer:
      override val howMany: Int = cloudSize
      override val atLeast: Int = minLength
      override val lastNWords: Int = windowSize
      override val ignore: Set[String] = ignoreSet

  test("empty input produces no output"):
    val sut = createSUT()
    sut.process(Iterator.empty)
    assert(sut.buffer.isEmpty)

  test("input shorter than window produces no output"):
    val sut = createSUT(windowSize = 5, minLength = 1)
    sut.process(Iterator("hello", "world", "foo", "bar"))
    assert(sut.buffer.isEmpty)

  test("output starts when window is full"):
    val sut = createSUT(windowSize = 3, minLength = 1)
    sut.process(Iterator("aaa", "bbb", "ccc"))
    assert(sut.buffer.size == 1)

  test("words below minLength are ignored"):
    val sut = createSUT(windowSize = 3, minLength = 4)
    sut.process(Iterator("ab", "cd", "abcd", "efgh", "ijkl"))
    // only abcd, efgh, ijkl qualify (3 words = windowSize), so exactly 1 output
    assert(sut.buffer.size == 1)
    val cloud = sut.buffer.head
    assert(cloud.map(_(0)).toSet == Set("abcd", "efgh", "ijkl"))

  test("enough qualifying words produce output with short words mixed in"):
    val sut = createSUT(windowSize = 2, minLength = 3)
    sut.process(Iterator("ab", "abc", "a", "def"))
    // qualifying: abc, def -> window fills at 2, so 1 output
    assert(sut.buffer.size == 1)
    val cloud = sut.buffer.head
    assert(cloud.map(_(0)).toSet == Set("abc", "def"))

  test("sliding window evicts oldest word"):
    val sut = createSUT(windowSize = 2, minLength = 1, cloudSize = 10)
    sut.process(Iterator("aaa", "bbb", "ccc"))
    // window fills at word 2 (aaa, bbb) -> output 1
    // word 3 (ccc): evict aaa, window = (bbb, ccc) -> output 2
    assert(sut.buffer.size == 2)
    val lastCloud = sut.buffer.last
    val words = lastCloud.map(_(0)).toSet
    assert(words == Set("bbb", "ccc"))
    // aaa should not be in the last cloud
    assert(!words.contains("aaa"))

  test("frequency updates correctly with repeated words"):
    val sut = createSUT(windowSize = 3, minLength = 1, cloudSize = 10)
    sut.process(Iterator("aaa", "aaa", "aaa"))
    assert(sut.buffer.size == 1)
    val cloud = sut.buffer.head
    assert(cloud == Seq(("aaa", 3)))

  test("cloud size limits output to howMany entries"):
    val sut = createSUT(windowSize = 5, minLength = 1, cloudSize = 2)
    sut.process(Iterator("aaa", "bbb", "ccc", "ddd", "eee"))
    assert(sut.buffer.size == 1)
    val cloud = sut.buffer.head
    assert(cloud.size == 2)

  test("cloud size larger than distinct words shows fewer entries"):
    val sut = createSUT(windowSize = 3, minLength = 1, cloudSize = 10)
    sut.process(Iterator("aaa", "bbb", "ccc"))
    assert(sut.buffer.size == 1)
    val cloud = sut.buffer.head
    // only 3 distinct words, cloudSize=10 but only 3 entries
    assert(cloud.size == 3)

  test("window size 1 produces one cloud per qualifying word"):
    val sut = createSUT(windowSize = 1, minLength = 1, cloudSize = 10)
    sut.process(Iterator("aaa", "bbb", "ccc"))
    assert(sut.buffer.size == 3)
    // each cloud has exactly one word with frequency 1
    assert(sut.buffer(0) == Seq(("aaa", 1)))
    assert(sut.buffer(1) == Seq(("bbb", 1)))
    assert(sut.buffer(2) == Seq(("ccc", 1)))

  test("ignore list filters words"):
    val sut = createSUT(windowSize = 2, minLength = 1, ignoreSet = Set("bbb"))
    sut.process(Iterator("aaa", "bbb", "ccc"))
    // bbb is ignored, qualifying: aaa, ccc -> fills window at 2, 1 output
    assert(sut.buffer.size == 1)
    val cloud = sut.buffer.head
    val words = cloud.map(_(0)).toSet
    assert(!words.contains("bbb"))
    assert(words == Set("aaa", "ccc"))

  test("full assignment example: -c 3 -l 2 -w 5"):
    val sut = createSUT(cloudSize = 3, minLength = 2, windowSize = 5)
    val input = "a b c aa bb cc aa bb aa bb a b c aa aa aa".split(" ")
    sut.process(input.iterator)

    // should produce exactly 6 outputs
    assert(sut.buffer.size == 6)

    // First output: bb:2, aa:2, cc:1 (all three present)
    val first = sut.buffer.head
    assert(first.size == 3)
    val firstMap = first.toMap
    assert(firstMap("aa") == 2)
    assert(firstMap("bb") == 2)
    assert(firstMap("cc") == 1)

    // Last output: aa:4, bb:1
    val last = sut.buffer.last
    assert(last.size == 2)
    assert(last.head == ("aa", 4))
    assert(last(1) == ("bb", 1))

end TestTopWords
