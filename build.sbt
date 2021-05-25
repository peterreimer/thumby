lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """thumby""",
    version := "2.8.x",
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      	"com.google.guava" % "guava" % "12.0",
 	"org.imgscalr" % "imgscalr-lib" % "4.2",
 	"org.apache.pdfbox" % "pdfbox" % "1.8.9",
 	"com.google.inject" % "guice" % "3.0",
      	guice,
      	ws
    )
  )
