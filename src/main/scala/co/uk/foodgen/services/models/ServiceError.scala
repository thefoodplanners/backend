package co.uk.foodgen.services.models

import zio.http.Status

enum ServiceError(val message: String, val statusCode: Status):
  case NotFound(override val message: String) extends ServiceError(message, Status.NotFound)
  case Unauthorised(override val message: String) extends ServiceError(message, Status.Unauthorized)
  case DomainValidationError(override val message: String) extends ServiceError(message, Status.BadRequest)
  case DatabaseError(override val message: String) extends ServiceError(message, Status.InternalServerError)
end ServiceError
