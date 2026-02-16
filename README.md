[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=22694662)
# Overview

Very simple example including an App and a few tests 
(illustrating different testing styles).

# Running the app

    sbt run

# Running the tests

    sbt test

# Determining test coverage

    sbt clean coverage test coverageReport
	
Now open this file in a web browser:

    target/scala-*/scoverage-report/index.html

Note that the Scala version number might vary depending on what's defined in the build configuration (`build.sbt`).    

# Running a Scala console

This allows you to explore the functionality of the classes in this
project in a Scala REPL while letting sbt set the classpath for you.

    sbt console
