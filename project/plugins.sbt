// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
//resolvers += "Typesafe repository" at "https://repo1.maven.org/maven2/com/typesafe/play/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8") //vorher 2.2.2

// For Eclipse integration
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
