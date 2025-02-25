package co.uk.foodgen.endpoints

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.{Id, Monad}
import co.uk.foodgen.models.User
import org.http4s.{ContextRequest, ContextRoutes, HttpRoutes}
import tsec.authentication.*
import tsec.mac.jca.{HMACSHA256, MacSigningKey}

import java.util.UUID
import scala.collection.mutable
import scala.concurrent.duration.*

object Authentication:
  private def dummyBackingStore[I, V](getId: V => I) = new BackingStore[IO, I, V]:
    private val storageMap = mutable.HashMap.empty[I, V]

    def put(elem: V): IO[V] =
      IO.pure(storageMap.getOrElseUpdate(getId(elem), elem))

    def get(id: I): OptionT[IO, V] = OptionT.fromOption[IO](storageMap.get(id))

    def update(v: V): IO[V] =
      IO.pure(storageMap.update(getId(v), v)).map(_ => v)

    def delete(id: I): IO[Unit] =
      IO.pure(storageMap.remove(id)).void

  private def identityBackingStore[I]() = new IdentityStore[IO, I, I]:
    def get(id: I): OptionT[IO, I] = OptionT.some[IO].apply(id)

  private val cookieBackingStore: BackingStore[IO, UUID, AuthenticatedCookie[HMACSHA256, User.Id]] =
    dummyBackingStore[UUID, AuthenticatedCookie[HMACSHA256, User.Id]](_.id)

  private val userStore: IdentityStore[IO, User.Id, User.Id] = identityBackingStore[User.Id]()

  private val settings: TSecCookieSettings = TSecCookieSettings(
    cookieName = "SESSION_KEY",
    secure = false,
    expiryDuration = 10.minutes,
    maxIdle = None // Rolling window expiration. Set this to a FiniteDuration if you intend to have one
  )

  // Our Signing key. Instantiate in a safe way using generateKey[F] where F[_]: Sync
  private val key: MacSigningKey[HMACSHA256] = HMACSHA256.generateKey[Id]

  private val cookieAuth =
    SignedCookieAuthenticator(
      settings,
      cookieBackingStore,
      userStore,
      key
    )

  private def contextRoutesToTSecAuthService[F[_]: Monad, I, A](
    routes: ContextRoutes[(I, A), F]
  ): TSecAuthService[I, A, F] =
    Kleisli { (secReq: SecuredRequest[F, I, A]) =>
      val ctxReq = ContextRequest[F, (I, A)]((secReq.identity, secReq.authenticator), secReq.request)
      routes.run(ctxReq)
    }

  extension (cxtRoutes: ContextRoutes[AuthInfo, IO])
    def withAuthentication: HttpRoutes[IO] = handler.liftService(contextRoutesToTSecAuthService(cxtRoutes))

  val handler = SecuredRequestHandler(cookieAuth)
