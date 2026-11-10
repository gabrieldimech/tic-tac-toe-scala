ThisBuild / scalaVersion := "3.9.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "tictactoe-scala",
    run / fork := true,
    run / connectInput := true,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all",
      "-Werror"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.scalameta" %% "munit" % "1.3.6" % Test,
      "org.scalameta" %% "munit-scalacheck" % "1.3.1" % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.2.1" % Test
    )
  )
