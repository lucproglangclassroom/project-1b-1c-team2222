# TopWords Design

## Overview

Transform the echotest-scala starter into a streaming word cloud tool called `topwords`. The program reads words from stdin and outputs updated word frequency clouds using a sliding window approach.

## Architecture

Follows the Observer pattern from the instructor's `iterators-scala` reference, separating computation from I/O.

### Core Traits (common.scala)

- `Observer[R]` - receives computation results via `update(result: R)`
- `StdoutObserver` - prints formatted word cloud to stdout, handles SIGPIPE
- `OutputToBuffer` - collects results in a mutable Buffer for testing

### Computation (TopWords.scala)

- `TopWordsComputation` trait - processes word iterator using sliding window
- Uses `mutable.Queue[String]` bounded to `lastNWords` for the sliding window
- Uses `mutable.Map[String, Int]` for word frequencies within the window
- For each qualifying word: enqueue, increment freq, if queue full then dequeue oldest and decrement its freq (remove if 0), then call `observer.update(top entries)`
- Result type: `Seq[(String, Int)]`

### Entry Point (Main.scala)

- Uses `mainargs` for CLI: `--cloud-size` (default 10), `--length-at-least` (default 6), `--window-size` (default 1000)
- Reads stdin as words via `Source.stdin.getLines.flatMap(_.split(regex))`
- Formats output as `w1: f1 w2: f2 ...`
- Logging via a third-party library

### Extra Credit: Ignore List

- `--ignore-file` CLI arg pointing to a file of words to skip (one per line)

### Testing

- `OutputToBuffer` observer collects structured `Seq[(String, Int)]` results
- Test cases: empty input, short input, full window, eviction, min-length filtering, ignore list
- ScalaTest with boundary cases for 80%+ coverage

## Key Constraints

- Constant space: bounded queue + map entries removed at count 0
- SIGPIPE: check `stdout.checkError()` after each print
- Scala 3 syntax with significant indentation
- `val` everywhere except where mutation is required
