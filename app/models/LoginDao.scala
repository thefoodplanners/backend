package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class LoginDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {
  def checkLoginDetails(loginDetails: LoginData): Future[Boolean] = {
    val newLoginDetails = loginDetails.copy(username = loginDetails.username.split("@").head)

    Future {
      db.withConnection { implicit conn =>
        val loginDataParser: RowParser[LoginData] = (
          SqlParser.str("Username") ~
            SqlParser.str("Password")
          ) map {
          case username ~ password =>
            LoginData(username, password)
        }

        val firstRow: Option[LoginData] =
          SQL"""
               SELECT * FROM Users
               WHERE Username=${newLoginDetails.username}
               AND Password=${newLoginDetails.password};
               """
            .as(loginDataParser.singleOpt)

        firstRow.contains(newLoginDetails)
      }
    }(databaseExecutionContext)
  }
}
