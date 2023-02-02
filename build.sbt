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
      "mysql" % "mysql-connector-java" % "8.0.32",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test,
      "com.github.nscala-time" %% "nscala-time" % "2.32.0"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
