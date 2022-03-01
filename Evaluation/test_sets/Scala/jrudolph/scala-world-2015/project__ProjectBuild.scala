import sbt._
import Keys._

import spray.revolver.RevolverPlugin._

object ProjectBuild extends Build {
  lazy val root =
    Project("root", file("."))
      .aggregate(backend, logService)

  // Akka Http based backend
  lazy val backend =
    Project("backend", file("backend"))
      .settings(Revolver.settings: _*)
      .settings(commonSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          "io.spray" %% "spray-json" % "1.3.2",
          "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
          "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "1.0",
          "com.lihaoyi" %% "pprint" % "0.3.6",
          "org.scala-lang" % "scala-reflect" % "2.11.7",
          "org.specs2" %% "specs2" % "2.3.12" % "test"
        )
      )

  lazy val logService =
    Project("log-service", file("log-service"))
      .settings(Revolver.settings: _*)
      .settings(commonSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-http-experimental" % "1.0"
        )
      )

  def commonSettings = Seq(
    scalaVersion := "2.11.7"
  ) ++ ScalariformSupport.formatSettings
}
