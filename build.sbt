name := "auth-service"

version := "0.1"

scalaVersion := "2.12.4"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.11"
)

libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.6"

libraryDependencies ++= List(
  "de.heikoseeberger" % "akka-http-json4s_2.12" % "1.16.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
)
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.1"

// https://mvnrepository.com/artifact/com.github.alaisi.pgasync/postgres-async-driver
libraryDependencies += "com.github.mauricio" %% "postgresql-async" % "0.2.21"

libraryDependencies += "commons-codec" % "commons-codec" % "1.11"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.2.2"