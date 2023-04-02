package models

import anorm._
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future

class UserDao @Inject()(
  db: Database,
  loginDao: LoginDao,
  calendarDao: CalendarDao
)(databaseExecutionContext: DatabaseExecutionContext) {

  val userPreferencesParser = (
    calendarDao.preferencesParser ~
      SqlParser.int("Target_calories")
  ) map {
    case preferences ~ targetCalories =>
      preferences.copy(targetCalories = Some(targetCalories))
  }

  def fetchTargetCalories(userId: String): Future[Int] = {
    Future {
      db.withConnection { implicit conn =>
          SQL"""
                SELECT Target_Calories FROM Preferences
                WHERE UserID = $userId;
                """.as(SqlParser.scalar[Int].single)
      }
    }(databaseExecutionContext)
  }

  def fetchPreferences(userId: String): Future[Preferences] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
              SELECT Vegan, Vegetarian, Keto, Lactose, Halal, Kosher, Dairy_free, Low_carbs, Gluten_free, Peanuts,
              Eggs, Fish, Tree_nuts, Soy, Target_calories
              FROM Preferences
              WHERE UserID = $userId
           """.as(userPreferencesParser.single)
      }
    }(databaseExecutionContext)
  }

  def updatePreferences(userId: String, prefs: Preferences): Future[Boolean] = {
    Future {
      db.withConnection { implicit conn =>
        SQL"""
              DELETE FROM Preferences
              WHERE UserID = $userId;
           """.execute()

        loginDao.insertPrefsQuery(userId, prefs).execute()
      }
    }(databaseExecutionContext)
  }
}
