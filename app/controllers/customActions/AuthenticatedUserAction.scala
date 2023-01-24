package controllers.customActions

import play.api.mvc.Results.Forbidden
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

class AuthenticatedUserAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {

  private val logger = play.api.Logger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    logger.debug("ENTERED AuthenticatedUserAction::invokeBlock")
    val maybeUsername = request.session.get("USERNAME")
    maybeUsername match {
      case None =>
        logger.debug("CAME INTO 'NONE'")
        Future.successful(Forbidden("Dude, you’re not logged in."))
      case Some(_) =>
        logger.debug("CAME INTO 'SOME'")
        val res: Future[Result] = block(request)
        res
    }
  }
}