import play.Project._

name := """thumby"""

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
 "com.google.guava" % "guava" % "12.0",
 "org.imgscalr" % "imgscalr-lib" % "4.2",
 "org.apache.pdfbox" % "pdfbox" % "1.1.0")

playJavaSettings
