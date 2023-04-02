package models

import anorm._
import org.mindrot.jbcrypt.BCrypt
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class LoginDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val checkLoginParser = (
    SqlParser.int("UserID") ~
      SqlParser.str("Password")
    ) map {
    case userId ~ hashedPassword =>
      (userId, hashedPassword)
  }

  def insertPrefsQuery(userId: String, prefs: Preferences): SimpleSql[Row] =
    SQL"""
          INSERT INTO Preferences(
            UserID, Vegan, Vegetarian, Keto,
            Lactose, Halal, Kosher, Dairy_free,
            Low_carbs, Gluten_free, Peanuts,
            Eggs, Fish, Tree_nuts, Soy,
            Target_Calories
          )
          VALUES (
            $userId,
            ${prefs.isVegan},
            ${prefs.isVegetarian},
            ${prefs.isKeto},
            ${prefs.isLactose},
            ${prefs.isHalal},
            ${prefs.isKosher},
            ${prefs.isDairyFree},
            ${prefs.isLowCarbs},
            ${prefs.isGlutenFree},
            ${prefs.isPeanuts},
            ${prefs.isEggs},
            ${prefs.isFish},
            ${prefs.isTreeNuts},
            ${prefs.isSoy},
            ${prefs.targetCalories.get}
          );
          """

  def checkLoginDetails(loginDetails: LoginData): Future[Option[Int]] = {
    val newLoginDetails = loginDetails.copy(username = loginDetails.username.split("@").head)

    Future {
      db.withConnection { implicit conn =>
        val hashedPassword: Option[(Int, String)] =
          SQL"""
               SELECT UserID, Password
               FROM Users
               WHERE Username = ${newLoginDetails.username};
               """
            .as(checkLoginParser.singleOpt)

        val isAuthorised = hashedPassword
          .filter { case(_, hashedPassword) => BCrypt.checkpw(loginDetails.password, hashedPassword)}
          .map(_._1)

        isAuthorised
      }
    }(databaseExecutionContext)
  }

  def addNewUser(registerData: RegisterData): Future[Boolean] = {
    Future {
      db.withConnection { implicit conn =>
        val hashedPassword = BCrypt.hashpw(registerData.password, BCrypt.gensalt())

        val usersInsert: Option[Long] =
          SQL"""
               INSERT INTO Users(Username, Password, Email)
               VALUES (${registerData.username}, $hashedPassword, ${registerData.email});
               """.executeInsert()

        val preferences = registerData.preferences

        insertPrefsQuery(usersInsert.get.toString, preferences).execute()

        usersInsert.nonEmpty
      }
    }(databaseExecutionContext)
  }
}
