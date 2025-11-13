package co.uk.foodgen.api

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.User
import pdi.jwt.{JwtAlgorithm, JwtZIOJson}
import zio.ZIO
import zio.http.*

object AuthMiddleware:
  val key = "secretkey"
  val algo = JwtAlgorithm.HS256

  def jwtAuthentication = HandlerAspect.customAuthProvidingZIO[Any, AuthInfo] { request =>
    ZIO
      .collectAll(
        request
          .header(Header.Authorization.Bearer)
          .map { bearer =>
            val jwt = bearer.token.stringValue
            val tryClaim = JwtZIOJson.decode(jwt, key, Seq(algo))
            val tryUserId = tryClaim.map { claim =>
              for
                subject <- claim.subject
                longId <- subject.toLongOption
                entityId = EntityId[User](longId)
              yield entityId
            }
            ZIO.fromTry(tryUserId)
          }
      )
      .map(_.flatten)
      .toResponseError
  }

  def decodeAuthToken(authToken: Header.Authorization.Bearer) =
    val jwt = authToken.token.stringValue
    val tryClaim = JwtZIOJson.decode(jwt, key, Seq(algo))
    val tryUserId = tryClaim.map { claim =>
      for
        subject <- claim.subject
        longId <- subject.toLongOption
        entityId = EntityId[User](longId)
      yield entityId
    }
    ZIO
      .fromTry(tryUserId)
      .flatMap(maybeUserId => ZIO.attempt(maybeUserId.get))
      .toResponseError
end AuthMiddleware
