
libraryDependencies ++= {
  val playMailerVersion = "6.0.1"
  val playJsonVersion = "2.6.9"
  Seq(
    "com.typesafe.play" %% "play-mailer" % playMailerVersion,
    "com.typesafe.play" %% "play-mailer-guice" % playMailerVersion,
    "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
    "com.typesafe.play" %% "play-json" % playJsonVersion,
    "com.typesafe.play" %% "play-json-joda" % playJsonVersion,
    specs2 % Test,
    ehcache,
    guice
  )
}
