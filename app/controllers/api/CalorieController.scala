package controllers.api

import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CalorieController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getTotalCalories(calories: List[Int]): Action[AnyContent] = Action {
    Ok(calories.sum.toString)
  }

}
