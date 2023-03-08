package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class LoginDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {
  def checkLoginDetails(loginDetails: LoginData): Future[(Boolean, Int)] = {
    val newLoginDetails = loginDetails.copy(username = loginDetails.username.split("@").head)

    Future {
      db.withConnection { implicit conn =>
        val firstRow: Option[Int] =
          SQL"""
               SELECT UserID FROM Users
               WHERE Username=${newLoginDetails.username}
               AND Password=${newLoginDetails.password};
               """
            .as(SqlParser.scalar[Int].singleOpt)

        val isAuthorised = firstRow.nonEmpty
        val userId = firstRow.getOrElse(0)

        (isAuthorised, userId)
      }
    }(databaseExecutionContext)
  }

  def addNewUser(registerData: RegisterData): Future[Option[Long]] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
             INSERT INTO Users(Username, Password, Email)
             VALUES (${registerData.username}, ${registerData.password}, ${registerData.email});
           """.executeInsert()
      }
    }(databaseExecutionContext)
  }
}
