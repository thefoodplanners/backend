package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class LoginDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {
  def checkLoginDetails(loginDetails: LoginData): Future[Boolean] = {
    Future {
      db.withConnection { implicit conn =>
        val loginDataParser: RowParser[LoginData] = (
          SqlParser.str("username") ~
            SqlParser.str("password")
          ) map {
          case username ~ password =>
            LoginData(username, password)
        }

        val firstRow: Option[LoginData] =
          SQL"""
               SELECT * FROM login_details
               WHERE username=${loginDetails.username}
               AND password=${loginDetails.password};
               """
            .as(loginDataParser.singleOpt)

        firstRow.contains(loginDetails)
      }
    }(databaseExecutionContext)
  }
}
