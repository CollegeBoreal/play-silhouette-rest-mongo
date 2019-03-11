resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= {
  val swaggerUIVersion = "3.6.1"
  Seq(
    "io.swagger" %% "swagger-play2" % "1.6.1-SNAPSHOT",
    "org.webjars" % "swagger-ui" % swaggerUIVersion,
    specs2 % Test,
    ehcache,
    guice
  )
}
