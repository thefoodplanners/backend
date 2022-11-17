lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """play-food-planner-app""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.10",
    libraryDependencies ++= Seq(
      jdbc,
      guice,
      "org.playframework.anorm" %% "anorm" % "2.7.0",
      "mysql" % "mysql-connector-java" % "8.0.30",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
