package co.uk.foodgen

import cats.data.EitherT
import cats.effect.IO
import co.uk.foodgen.service.errorModels.ServiceError
import doobie.ConnectionIO
import doobie.implicits.toConnectionIOOps
import doobie.util.transactor.Transactor

package object service:
  type ServiceResult[A] = EitherT[IO, ServiceError, A]

  extension [A](conn: ConnectionIO[A])
    def tx(using transactor: Transactor[IO]): IO[A] = conn.transact(transactor)
    def validTx(using transactor: Transactor[IO]): ServiceResult[A] = EitherT.liftF(conn.tx)
    def invalidTx(serviceError: => ServiceError): ServiceResult[A] = EitherT.leftT(serviceError)

  extension [A](conn: ConnectionIO[Option[A]])
    def orNotFound(serviceError: ServiceError)(using transactor: Transactor[IO]): ServiceResult[A] =
      EitherT.fromOptionF(conn.tx, serviceError)
    def failIfFound(serviceError: ServiceError)(using transactor: Transactor[IO]): ServiceResult[Unit] =
      EitherT.apply(conn.tx.map(_.invert.toRight(serviceError)))

  extension [A](opt: Option[A]) private def invert: Option[Unit] = opt.fold(Some(()))(_ => None)

end service
