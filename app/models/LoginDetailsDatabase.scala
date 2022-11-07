package models

import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class LoginDetailsDatabase @Inject()(db: Database) (implicit databaseExecutionContext: ExecutionContext) {
  def checkLoginDetails(loginDetails: LoginData): Future[Boolean] = {
    Future {
      db.withConnection { implicit conn =>
        // do whatever you need with the db connection
        val statement = conn.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM login_details;")
        resultSet.next()
        val storedUsername = Option(resultSet.getString("username"))
        val storedPassword = Option(resultSet.getString("password"))

        storedUsername.contains(loginDetails.username) && storedPassword.contains(loginDetails.password)
      }
    }(databaseExecutionContext)
  }
}
