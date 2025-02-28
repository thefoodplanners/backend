package co.uk

import cats.effect.{IO, Resource}
import co.uk.foodgen.Server.setTransactor
import co.uk.foodgen.endpoints.Authentication
import co.uk.foodgen.models.User
import com.dimafeng.testcontainers.{Container, PostgreSQLContainer}
import doobie.syntax.connectionio.toConnectionIOOps
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import io.circe.Json
import org.http4s.Method.GET
import org.http4s.{Request, RequestCookie, Uri}
import org.testcontainers.utility.DockerImageName
import weaver.{Expectations, TestName}

import scala.io.Source

package object foodgen:
  private val containerDef = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:latest")
  )

  def containerResource[C <: Container](container: IO[C]): Resource[IO, C] =
    Resource.make(container.flatTap { container =>
      IO.blocking(container.start())
    })(c => IO.blocking(c.stop()))

  def testWithDb[A](
    app: Transactor[IO] => A
  )(test: TestName => IO[Expectations] => Unit)(name: TestName)(
    testLogic: A => IO[Expectations]
  ): Unit =
    val txRes =
      for
        container <- containerResource(IO.pure(containerDef.createContainer()))
        transactor <- setTransactor(
          container.driverClassName,
          container.jdbcUrl,
          container.username,
          container.password
        )
        _ <- initDb(transactor)
      yield transactor

    test(name)(txRes.use(app andThen testLogic))

  def initDb(transactor: Transactor[IO]): Resource[IO, Unit] =
    for
      source <- Resource.fromAutoCloseable(IO(Source.fromResource("init.sql")))
      query = Fragment.const0(source.getLines().mkString(" "))
      _ <- Resource.eval(query.update.run.transact(transactor))
    yield ()

  def decryptUserIdFromCookieOrFail(cookie: RequestCookie): IO[User.Id] =
    val dummyRequest = Request[IO](GET, Uri.unsafeFromString("/dummy")).addCookie(cookie)
    Authentication.handler.authenticator
      .extractAndValidate(dummyRequest)
      .map(_.identity)
      .value
      .map(_.get)

  extension (json: Json)
    def removeJsonFields(fieldsToRemove: Set[String]): Json =
      json.fold(
        json,
        _ => json,
        _ => json,
        _ => json,
        arr => Json.fromValues(arr.map(_.removeJsonFields(fieldsToRemove))),
        obj => obj.filterKeys(!fieldsToRemove.contains(_)).mapValues(_.removeJsonFields(fieldsToRemove)).toJson
      )

end foodgen
