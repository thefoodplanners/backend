lazy val oracleVersion = "23.3.0.23.09"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin, DockerPlugin)
  .settings(
    name := "foodgen-backend",
    version := "1.0",
    scalaVersion := "2.13.10",
    swaggerDomainNameSpaces := Seq("models"),
    Docker / packageName := "foodgen/backend",
    dockerExposedPorts ++= Seq(9000),
    libraryDependencies ++= Seq(
      jdbc,
      jdbc % Test,
      guice,
      "org.playframework.anorm" %% "anorm" % "2.7.0",
      "com.oracle.database.jdbc" % "ojdbc10" % "19.22.0.0",
      "com.github.nscala-time" %% "nscala-time" % "2.32.0",
      "org.webjars" % "swagger-ui" % "4.18.1",
      "org.mindrot" % "jbcrypt" % "0.4",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
