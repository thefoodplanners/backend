package controllers.api

import models.SearchDao
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

class SearchController @Inject()(
  cc: ControllerComponents,
  database: SearchDao,
  calendarController: CalendarController
)
  (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def searchForRecipes(query: String): Action[AnyContent] = Action.async {
    database.searchForRecipes(query)
      .flatMap(calendarController.mealSlotImageRefToString)
      .map(recipesWithImg => Ok(Json.toJson(recipesWithImg)))
  }

}