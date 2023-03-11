package models

import anorm.{ SqlParser, SqlStringInterpolation }
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future

class UserDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {
  def fetchMaxCalories(userId: String): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
          SQL"""
                SELECT Max_Calories FROM Preferences
                WHERE UserID = $userId;
                """.as(SqlParser.scalar[Int].single)
      }
    }(databaseExecutionContext)
  }
}
