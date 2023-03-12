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

  def addNewUser(registerData: RegisterData): Future[Option[(Long, Long)]] = {
    Future {
      db.withConnection { implicit conn =>
        val usersInsert: Option[Long] = SQL"""
             INSERT INTO Users(Username, Password, Email)
             VALUES (${registerData.username}, ${registerData.password}, ${registerData.email});
           """.executeInsert()

        val preferences = registerData.preferences

        val prefInsert: Option[Long] = SQL"""
             INSERT INTO Preferences(UserID, Vegan, Vegetarian,
             Keto, Lactose, Halal, Kosher, Dairy_free, Low_carbs,
             Gluten_free, Peanuts, Eggs, Fish, Tree_nuts, Soy, Max_Calories)
             VALUES (${preferences.isVegan}, ${preferences.isVegetarian},
             ${preferences.isKeto}, ${preferences.isLactose}, ${preferences.isHalal},
             ${preferences.isKosher}, ${preferences.isDairyFree}, ${preferences.isLowCarbs});
           """.executeInsert()

        for {
          user <- usersInsert
          pref <- prefInsert
        } yield (user, pref)
      }
    }(databaseExecutionContext)
  }
}
