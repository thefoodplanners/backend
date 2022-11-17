package controllers

import play.api.mvc._

import javax.inject._

class HomePageController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def logout = ???
//    Redirect(routes.UserController)
//      .flashing("info" -> "You are not logged out.")
//      .withNewSession
}