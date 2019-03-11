name := "play-silhouette-rest-mongo"
 
version := "1.0" 

lazy val `play-silhouette-rest-mongo` = (project in file(".")).enablePlugins(PlayScala)
scalacOptions ++= Seq("-deprecation", "-language:_")

scalaVersion := "2.12.6"

val reactiveMongoVersion = "0.13.0-play26"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVersion,
  specs2 % Test
)

unmanagedResourceDirectories in Test += (baseDirectory.value / "target/web/public/test")

resolvers += Resolver.jcenterRepo
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "atlassian-maven" at "https://maven.atlassian.com/content/repositories/atlassian-public"
