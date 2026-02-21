package edu.luc.cs.cs371.topwords

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
import java.nio.file.Files

class TestMain extends AnyFunSuite:

  import scala.language.unsafeNulls

  private def runMainCapture(
    input: String,
    cloudSize: Int = 10,
    lengthAtLeast: Int = 6,
    windowSize: Int = 1000,
    ignoreFile: Option[String] = None
  ): String =
    val stdin = new ByteArrayInputStream(input.getBytes)
    val oldIn = System.in
    try
      System.setIn(stdin)
      val baos = new ByteArrayOutputStream()
      Console.withOut(baos):
        val oldSysOut = System.out
        System.setOut(new PrintStream(baos))
        try
          Main.run(cloudSize = cloudSize, lengthAtLeast = lengthAtLeast, windowSize = windowSize, ignoreFile = ignoreFile)
        finally
          System.setOut(oldSysOut)
      baos.toString.trim
    finally
      System.setIn(oldIn)

  test("Main.run produces correct output for simple input"):
    val output = runMainCapture(
      input = "aaa bbb ccc aaa bbb aaa\n",
      cloudSize = 3, lengthAtLeast = 1, windowSize = 3
    )
    val lines = output.split("\n")
    assert(lines.length == 4)

  test("Main.run with empty input produces no output"):
    val output = runMainCapture(
      input = "",
      cloudSize = 10, lengthAtLeast = 1, windowSize = 5
    )
    assert(output.isEmpty)

  test("Main.run respects lengthAtLeast parameter"):
    val output = runMainCapture(
      input = "ab cd abcdef ghijkl mnopqr\n",
      cloudSize = 10, lengthAtLeast = 6, windowSize = 3
    )
    val lines = output.split("\n")
    assert(lines.length == 1)
    assert(lines(0).contains("abcdef"))
    assert(lines(0).contains("ghijkl"))
    assert(lines(0).contains("mnopqr"))
    assert(!lines(0).contains("ab:"))
    assert(!lines(0).contains("cd:"))

  test("Main.run respects cloudSize parameter"):
    val output = runMainCapture(
      input = "aaa bbb ccc ddd eee\n",
      cloudSize = 2, lengthAtLeast = 1, windowSize = 5
    )
    val lines = output.split("\n")
    assert(lines.length == 1)
    val parts = lines(0).split(" ")
    assert(parts.length == 4)

  test("Main.run with ignoreFile filters words from file"):
    val tempFile = Files.createTempFile("ignore", ".txt")
    Files.writeString(tempFile, "bbb\nccc\n"): Unit
    try
      val output = runMainCapture(
        input = "aaa bbb ccc ddd eee\n",
        cloudSize = 10, lengthAtLeast = 1, windowSize = 3,
        ignoreFile = Some(tempFile.toString)
      )
      val lines = output.split("\n")
      assert(lines.length == 1)
      assert(!lines(0).contains("bbb"))
      assert(!lines(0).contains("ccc"))
      assert(lines(0).contains("aaa"))
      assert(lines(0).contains("ddd"))
      assert(lines(0).contains("eee"))
    finally
      Files.deleteIfExists(tempFile): Unit

  test("Main.run with no ignoreFile uses empty ignore set"):
    val output = runMainCapture(
      input = "aaa bbb ccc\n",
      cloudSize = 10, lengthAtLeast = 1, windowSize = 3,
      ignoreFile = None
    )
    val lines = output.split("\n")
    assert(lines.length == 1)
    assert(lines(0).contains("aaa"))
    assert(lines(0).contains("bbb"))
    assert(lines(0).contains("ccc"))

  test("Main.run throws on non-positive cloudSize"):
    assertThrows[IllegalArgumentException]:
      Main.run(cloudSize = 0, lengthAtLeast = 1, windowSize = 1)

  test("Main.run throws on non-positive lengthAtLeast"):
    assertThrows[IllegalArgumentException]:
      Main.run(cloudSize = 1, lengthAtLeast = 0, windowSize = 1)

  test("Main.run throws on non-positive windowSize"):
    assertThrows[IllegalArgumentException]:
      Main.run(cloudSize = 1, lengthAtLeast = 1, windowSize = 0)

  test("Main.run handles multiline input"):
    val output = runMainCapture(
      input = "aaa bbb\nccc ddd\neee fff\n",
      cloudSize = 10, lengthAtLeast = 1, windowSize = 3
    )
    val lines = output.split("\n")
    assert(lines.length == 4)

  test("Main.run splits on non-alpha characters"):
    val output = runMainCapture(
      input = "hello-world foo.bar baz!qux\n",
      cloudSize = 10, lengthAtLeast = 1, windowSize = 6
    )
    val lines = output.split("\n")
    assert(lines.length == 1)
    assert(lines(0).contains("hello"))
    assert(lines(0).contains("world"))
    assert(lines(0).contains("foo"))
    assert(lines(0).contains("bar"))
    assert(lines(0).contains("baz"))
    assert(lines(0).contains("qux"))

  test("Main.run with ignoreFile containing blank lines and whitespace"):
    val tempFile = Files.createTempFile("ignore", ".txt")
    Files.writeString(tempFile, "bbb\n\n  \nccc\n"): Unit
    try
      val output = runMainCapture(
        input = "aaa bbb ccc ddd eee\n",
        cloudSize = 10, lengthAtLeast = 1, windowSize = 3,
        ignoreFile = Some(tempFile.toString)
      )
      assert(!output.contains("bbb"))
      assert(!output.contains("ccc"))
    finally
      Files.deleteIfExists(tempFile): Unit

  test("Main.run output contains frequency counts"):
    val output = runMainCapture(
      input = "aaa aaa aaa bbb bbb ccc\n",
      cloudSize = 10, lengthAtLeast = 1, windowSize = 6
    )
    val lines = output.split("\n")
    assert(lines.length == 1)
    assert(lines(0).contains("aaa: 3"))
    assert(lines(0).contains("bbb: 2"))
    assert(lines(0).contains("ccc: 1"))

  test("Main.run with default parameters uses cloudSize=10 lengthAtLeast=6 windowSize=1000"):
    val words = (1 to 1010).map(i => f"word${i}%04d").mkString(" ") + "\n"
    val stdin = new ByteArrayInputStream(words.getBytes)
    val oldIn = System.in
    try
      System.setIn(stdin)
      val baos = new ByteArrayOutputStream()
      Console.withOut(baos):
        val oldSysOut = System.out
        System.setOut(new PrintStream(baos))
        try
          Main.run()
        finally
          System.setOut(oldSysOut)
      val output = baos.toString.trim
      val lines = output.split("\n")
      assert(lines.length == 11)
      for line <- lines do
        val count = line.split(" ").count(_.contains(":"))
        assert(count <= 10)
    finally
      System.setIn(oldIn)

  test("Main.main parses CLI args and runs"):
    val input = "abcdef ghijkl mnopqr\n"
    val stdin = new ByteArrayInputStream(input.getBytes)
    val oldIn = System.in
    try
      System.setIn(stdin)
      val baos = new ByteArrayOutputStream()
      Console.withOut(baos):
        val oldSysOut = System.out
        System.setOut(new PrintStream(baos))
        try
          Main.main(Array("-c", "3", "-l", "1", "-w", "3"))
        finally
          System.setOut(oldSysOut)
      val output = baos.toString.trim
      val lines = output.split("\n")
      assert(lines.length == 1)
      assert(lines(0).contains("abcdef"))
      assert(lines(0).contains("ghijkl"))
      assert(lines(0).contains("mnopqr"))
    finally
      System.setIn(oldIn)

  test("Main.main with no args uses defaults"):
    val input = "short\n"
    val stdin = new ByteArrayInputStream(input.getBytes)
    val oldIn = System.in
    try
      System.setIn(stdin)
      val baos = new ByteArrayOutputStream()
      Console.withOut(baos):
        val oldSysOut = System.out
        System.setOut(new PrintStream(baos))
        try
          Main.main(Array.empty[String])
        finally
          System.setOut(oldSysOut)
      val output = baos.toString.trim
      assert(output.isEmpty)
    finally
      System.setIn(oldIn)

end TestMain
