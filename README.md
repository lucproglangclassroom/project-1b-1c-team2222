[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=22694662)

# TopWords - Streaming Word Cloud

A streaming word cloud tool that reads words from stdin and outputs updated word frequency statistics using a sliding window approach.

## Usage

    ./target/universal/stage/bin/topwords-scala --cloud-size howmany --length-at-least minlength --window-size lastnwords

Defaults: cloud-size=10, length-at-least=6, window-size=1000.

### Options

- `-c, --cloud-size` - Number of words to show in the cloud (default: 10)
- `-l, --length-at-least` - Minimum word length to consider (default: 6)
- `-w, --window-size` - Sliding window size of most recent qualifying words (default: 1000)
- `-i, --ignore-file` - Path to file of words to ignore, one per line (extra credit)

## Building and Running

    sbt stage
    ./target/universal/stage/bin/topwords-scala < input.txt

### Example

    echo "a b c aa bb cc aa bb aa bb a b c aa aa aa" | tr ' ' '\n' | ./target/universal/stage/bin/topwords-scala -c 3 -l 2 -w 5

Output:

    aa: 2 bb: 2 cc: 1
    aa: 2 bb: 2 cc: 1
    aa: 2 bb: 2 cc: 1
    aa: 3 bb: 2
    aa: 3 bb: 2
    aa: 4 bb: 1

## Running the Tests

    sbt test

## Determining Test Coverage

    sbt clean coverage test coverageReport

Open the report in a browser:

    target/scala-3.7.4/scoverage-report/index.html

Current coverage: 100% statement, 100% branch.

## Extra Credit Features

- **Ignore list**: Use `--ignore-file` to specify a file of words to ignore regardless of length. The file should contain one word per line.

## Architecture

Uses a **purely functional pipeline** following the instructor's `iterators-scala` functional modular pattern:

- `TopWordsComputation` trait — pure function `Iterator[String] => Iterator[Seq[(String, Int)]]` using `Iterator.scanLeft` with an immutable `WindowState(Queue, Map)` accumulator
- `formatCloud` — formats a word cloud as a space-separated string
- `runWithStdIO` — consumes the result iterator, printing to stdout with SIGPIPE handling via `takeWhile(!System.out.checkError())`
- `Main` — CLI entry point with mainargs, wires stdin → computation → runWithStdIO

No mutable state (`var`, `mutable.Queue`, `mutable.Map`) or Observer pattern. The computation returns a lazy `Iterator[Result]` that callers consume directly — tests call `.toList`, production pipes through `runWithStdIO`.

## Scalability

Runs in constant space using immutable `Queue` (amortized O(1) enqueue/dequeue) and immutable `Map` (O(log n) update). The `scanLeft` pipeline processes one word at a time, holding only the current `WindowState`. Verified with:

    yes helloworld | ./target/universal/stage/bin/topwords-scala > /dev/null

## Libraries Used

- [mainargs](https://github.com/com-lihaoyi/mainargs) - Command-line argument parsing
- [scala-logging](https://github.com/lightbend-labs/scala-logging) + [logback](https://logback.qos.ch/) - Logging
- [ScalaTest](https://www.scalatest.org/) + [ScalaCheck](https://www.scalacheck.org/) - Testing
- [sbt-native-packager](https://github.com/sbt/sbt-native-packager) - Binary packaging
- [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) - Code coverage
