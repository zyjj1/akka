/*
 * Copyright (C) 2017-2018 Lightbend Inc. <http://www.lightbend.com/>
 */

package akka

import sbt._
import sbt.Keys._

object Release9 extends AutoPlugin {

  lazy val CompileRelease9 = config("CompileRelease9").extend(Compile)

  val compileRelease9Settings = Seq(
    // following the scala-2.12, scala-sbt-1.0, ... convention
    unmanagedSourceDirectories := Seq(
      (Compile / sourceDirectory).value / "scala-release-9",
      (Compile / sourceDirectory).value / "java-release-9"
    ),
    scalacOptions := AkkaBuild.DefaultScalacOptions ++ Seq("-release", "9"),
    javacOptions := AkkaBuild.DefaultJavacOptions ++ Seq("--release", "9")
  )

  val compileSettings = Seq(
    Compile / packageBin / mappings ++=
      (CompileRelease9 / products).value.flatMap(Path.allSubpaths)
  )

  override def trigger = noTrigger
  override def projectConfigurations = Seq(CompileRelease9)
  override lazy val projectSettings =
    inConfig(CompileRelease9)(Defaults.compileSettings) ++
    inConfig(CompileRelease9)(compileRelease9Settings) ++
    compileSettings
}
