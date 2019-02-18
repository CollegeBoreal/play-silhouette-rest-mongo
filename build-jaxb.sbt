// J2EE JAXB Related
libraryDependencies ++= {
  val xmlBindVersion = "2.2.11"
  Seq(
    "javax.xml.bind" % "jaxb-api" % xmlBindVersion,
    "com.sun.xml.bind" % "jaxb-core" % xmlBindVersion,
    "com.sun.xml.bind" % "jaxb-impl" % xmlBindVersion
  )
}
