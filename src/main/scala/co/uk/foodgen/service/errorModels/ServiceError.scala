package co.uk.foodgen.service.errorModels

import sttp.model.StatusCode
import sttp.tapir.*

import scala.compiletime.erasedValue

enum ServiceError:
  case BadRequest(message: String)
  case NotFound(message: String)
  case Unauthorized

object ServiceError:
  inline def toStatusCode[T <: ServiceError] = inline erasedValue[T] match
    case _: ServiceError.BadRequest =>
      oneOfVariant(statusCode(StatusCode.BadRequest).and(stringBody).mapTo[ServiceError.BadRequest])
    case _: ServiceError.NotFound =>
      oneOfVariant(statusCode(StatusCode.NotFound).and(stringBody).mapTo[ServiceError.NotFound])
    case ServiceError.Unauthorized =>
      oneOfVariantSingletonMatcher(statusCode(StatusCode.Unauthorized))(ServiceError.Unauthorized)
