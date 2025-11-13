package co.uk.foodgen.api

import co.uk.foodgen.api.endpoints.UserEndpoints.*
import co.uk.foodgen.payload.responses.{UserDietaryRequirementsResponse, UserTargetCaloriesResponse}
import co.uk.foodgen.services.UserService
import zio.ZIO
import zio.http.*

import javax.sql.DataSource

object UserRoutes extends APIRoute:
  override val routes = Routes(
    getUserTargetCalories.implement { _ =>
      (for
        userId <- ZIO.service[AuthInfo]
        targetCalories <- UserService.fetchTargetCalories(userId)
        responseBody = UserTargetCaloriesResponse(targetCalories)
      yield responseBody).toRoutesError
    },
    getUserDietaryRequirements.implement { _ =>
      (for
        userId <- ZIO.service[AuthInfo]
        dietReqs <- UserService.fetchDietaryRequirements(userId)
        responseBody = UserDietaryRequirementsResponse(dietReqs)
      yield responseBody).toRoutesError
    }
  )
end UserRoutes
