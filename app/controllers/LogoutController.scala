package controllers

import play.api.mvc._

import javax.inject._

class LogoutController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def logout: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.LoginController.showLoginForm)
      .flashing("info" -> "You are now logged out.")
      .withNewSession
  }

}