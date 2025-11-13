val QuillVersion = "4.8.6"
val QuillBaseVersion = "4.8.5"
val TestContainersVersion = "0.43.6"
val LogbackVersion = "1.5.20"

lazy val root = (project in file("."))
  .settings(
    organization := "co.uk.foodgen",
    name := "chai",
    version := "1.0.0",
    scalaVersion := "3.3.3",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.22",
      "dev.zio" %% "zio-http" % "3.5.1",
      "dev.zio" %% "zio-schema" % "1.7.5",
      "dev.zio" %% "zio-schema-json" % "1.7.5",
      "dev.zio" %% "zio-json" % "0.7.45",
      "dev.zio" %% "zio-prelude" % "1.0.0-RC42",
      "de.mkammerer" % "argon2-jvm" % "2.12",
      "com.github.jwt-scala" %% "jwt-zio-json" % "11.0.3",
      "io.getquill" %% "quill-jdbc-zio" % QuillVersion,

      "org.postgresql" % "postgresql" % "42.7.8",
      "dev.zio" %% "zio-test" % "2.1.22" % Test,

      "com.dimafeng" %% "testcontainers-scala" % TestContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % TestContainersVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("io", "getquill", _*) => MergeStrategy.first
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
        MergeStrategy.singleOrError
      case PathList("META-INF", "resources", "webjars", "swagger-ui", _*) =>
        MergeStrategy.singleOrError
      case "module-info.class" | PathList("META-INF", _*) => MergeStrategy.discard
      case x                       => (assembly / assemblyMergeStrategy).value.apply(x)
    },
  )
