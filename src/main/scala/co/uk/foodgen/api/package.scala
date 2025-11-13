package co.uk.foodgen

import co.uk.foodgen.domain.Entity.EntityId
import co.uk.foodgen.domain.User
import co.uk.foodgen.services.models.ServiceError
import zio.ZIO
import zio.http.{ErrorResponseConfig, Response}

import scala.annotation.targetName

package object api:
  type AuthInfo = EntityId[User]

  val errorConfig = ErrorResponseConfig(
    withErrorBody = true,
    withStackTrace = true,
    maxStackTraceDepth = 0,
    errorFormat = ErrorResponseConfig.ErrorFormat.Json
  )

  extension [R, E <: Throwable, A](effect: ZIO[R, E, A])
    def toResponseError: ZIO[R, Response, A] =
      effect.mapError(th => Response.fromThrowable(th, errorConfig))
    def toRoutesError: ZIO[R, Nothing, A] =
      effect.tapErrorCause(cause => ZIO.logErrorCause("Handler failed", cause)).orDie

  extension [R, A](effect: ZIO[R, ServiceError, A])
    @targetName("toResponseErrorFromServiceError")
    def toResponseError: ZIO[R, Response, A] =
      effect.mapError(se => Response.status(se.statusCode))

end api
