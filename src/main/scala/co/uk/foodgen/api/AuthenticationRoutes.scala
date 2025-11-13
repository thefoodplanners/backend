package co.uk.foodgen.api

import co.uk.foodgen.api.endpoints.AuthenticationEndpoints.*
import co.uk.foodgen.payload.requests.{LoginRequest, RegisterRequest}
import co.uk.foodgen.services.AuthenticationService
import pdi.jwt.{JwtClaim, JwtZIOJson}
import zio.ZIO
import zio.http.*

import java.time.Instant
import javax.sql.DataSource

object AuthenticationRoutes extends APIRoute:
  override val unauthenticatedRoutes = Routes(
    registerEndpoint.implement { (body: RegisterRequest) =>
      (for _ <- AuthenticationService.register(
          body.email,
          body.username,
          body.password,
          body.targetCalories,
          body.dietaryRequirements
        )
      yield ()).toRoutesError
    },
    loginEndpoint.implement { (body: LoginRequest) =>
      (for
        maybeUserId <- AuthenticationService.login(body.username, body.password)
        jwtClaimOrFail = maybeUserId
          .map { userId =>
            val claim = JwtClaim(
              subject = Some(userId.toString),
              expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond),
              issuedAt = Some(Instant.now.getEpochSecond)
            )
            JwtZIOJson.encode(claim, AuthMiddleware.key, AuthMiddleware.algo)
          }
          .toRight(Exception("Your username/password is incorrect"))
        jwtClaim <- ZIO.fromEither(jwtClaimOrFail)
        header = Header.Authorization.Bearer(jwtClaim)
      yield header).toRoutesError
    }
  )

end AuthenticationRoutes
