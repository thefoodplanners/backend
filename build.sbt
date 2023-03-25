lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerPlugin)
  .settings(
    name := "FoodGen-backend",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.10",
    swaggerDomainNameSpaces := Seq("models"),
    libraryDependencies ++= Seq(
      jdbc,
      jdbc % Test,
      guice,
      "org.playframework.anorm" %% "anorm" % "2.7.0",
      "mysql" % "mysql-connector-java" % "8.0.32",
      "com.github.nscala-time" %% "nscala-time" % "2.32.0",
      "com.hierynomus" % "sshj" % "0.35.0",
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
