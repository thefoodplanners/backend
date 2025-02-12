// https://github.com/http4s/http4s/security/advisories/GHSA-52cf-226f-rhr6
val Http4sVersion = "0.23.2"
val TapirVersion = "1.11.14"
// Updating circe version will break tapir-swagger-ui-bundle
val CirceVersion = "0.14.10"
val NeoTypeVersion = "0.3.14"
val DoobieVersion = "1.0.0-RC7"
val LogbackVersion = "1.5.16"
val TestContainersVersion = "0.41.8"

lazy val root = (project in file("."))
  .settings(
    organization := "co.uk.foodgen",
    name := "chai",
    version := "1.0.0",
    scalaVersion := "3.3.3",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.github.kitlangton" %% "neotype" % NeoTypeVersion,
      "io.github.kitlangton" %% "neotype-circe" % NeoTypeVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "io.scalaland" %% "chimney" % "1.7.3",
      "org.mindrot" % "jbcrypt" % "0.4",
      "org.reactormonk" % "cryptobits" % "1.3.1" cross CrossVersion.for3Use2_13,
      "com.disneystreaming" %% "weaver-cats" % "0.8.4" % Test,
      "com.dimafeng" %% "testcontainers-scala" % TestContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % TestContainersVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
        MergeStrategy.singleOrError
      case PathList("META-INF", "resources", "webjars", "swagger-ui", _*) =>
        MergeStrategy.singleOrError
      case PathList("META-INF", x, _*) if x.toLowerCase == "services" => MergeStrategy.filterDistinctLines
      case "module-info.class" | PathList("META-INF", _*) => MergeStrategy.discard
      case x                       => (assembly / assemblyMergeStrategy).value.apply(x)
    },
  )
