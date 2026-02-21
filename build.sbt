name := "topwords-scala"

version := "0.1"

libraryDependencies ++= Seq(
  "com.lihaoyi"                %% "mainargs"        % "0.7.8",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5",
  "ch.qos.logback"              % "logback-classic"  % "1.5.6",
  "org.scalatest"              %% "scalatest"        % "3.2.19"   % Test,
  "org.scalacheck"             %% "scalacheck"       % "1.19.0"   % Test,
  "org.scalatestplus"          %% "scalacheck-1-18"  % "3.2.19.0" % Test
)

enablePlugins(JavaAppPackaging)
