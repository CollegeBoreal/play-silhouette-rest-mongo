resolvers += "iheartradio-maven" at "https://dl.bintray.com/iheartradio/maven"

libraryDependencies ++= {
  val silhouetteVersion = "5.0.3"
  Seq(
    "com.mohiva" %% "play-silhouette" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
    "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % "test",
    "com.iheart" %% "ficus" % "1.4.3",
    "net.codingwell" %% "scala-guice" % "4.1.1",
    specs2 % Test,
    ehcache,
    guice
  )
}