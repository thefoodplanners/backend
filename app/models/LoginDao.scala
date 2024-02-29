package models

import anorm._
import org.mindrot.jbcrypt.BCrypt
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * DAO class for accessing the SQL database relating to login queries.
 */
class LoginDao @Inject()(db: Database)(databaseExecutionContext: DatabaseExecutionContext) {

  private val checkLoginParser = (
    SqlParser.int("UserID") ~
      SqlParser.str("Password")
    ) map {
    case userId ~ hashedPassword =>
      (userId, hashedPassword)
  }

  /**
   * Adds new preferences row to Preferences table in database.
   *
   * @param userId Id of user.
   * @param prefs  Preferences case class.
   * @return Sql row that can be executed.
   */
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

  /**
   * Checks whether provided username and password is the same as in the database.
   *
   * @param loginDetails LoginData case class.
   * @return User id if check is valid, None if it is not.
   */
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
          .filter { case (_, hashedPassword) => BCrypt.checkpw(loginDetails.password, hashedPassword) }
          .map(_._1)
        isAuthorised
      }
    }(databaseExecutionContext)
  }

  /**
   * Adds new user to database, along with preferences.
   *
   * @param registerData RegisterData case class.
   * @return Whether user was successfully added or not.
   */
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
