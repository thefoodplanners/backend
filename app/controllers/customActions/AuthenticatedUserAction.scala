package controllers.customActions

import models.SESSION_KEY
import play.api.Logging
import play.api.mvc.Results.Forbidden
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }

class AuthenticatedUserAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) with Logging {

  override def invokeBlock[A](
    request: Request[A],
    block: Request[A] => Future[Result]
  ): Future[Result] =
    request
      .session
      .get(SESSION_KEY)
      .map(_ => block(request))
      .getOrElse(Future.successful(Forbidden))
}