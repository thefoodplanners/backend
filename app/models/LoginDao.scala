package models

import play.api.db.Database

import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class LoginDao @Inject()(db: Database) (implicit databaseExecutionContext: DatabaseExecutionContext) {

  def checkLoginDetails(loginDetails: LoginData): Future[Boolean] = {
    Future {
      db.withConnection { conn =>
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

  def fetchAllRecipes: Future[List[Recipe]] = {
    Future {
      db.withConnection { conn =>
        // do whatever you need with the db connection
        val statement = conn.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM recipe;")
        val recipeList = new ListBuffer[Recipe]
        while (resultSet.next()) {
          val storedId = resultSet.getString("recipeid")
          val storedName = resultSet.getString("name")
          val storedType = resultSet.getString("type")
          val storedDesc = resultSet.getString("description")
          val storedTime = resultSet.getString("time")
          val storedDifficulty = resultSet.getString("difficulty")
          val storedIng = resultSet.getString("ingredients")
          val storedInstr = resultSet.getString("instructions")
          val storedCal = resultSet.getString("calories")
          val storedFats = resultSet.getString("fats")

          val recipe = Recipe(storedName, storedType, storedDesc, storedTime, storedDifficulty, storedIng, storedInstr, storedCal, storedFats)
          recipeList += recipe
        }

        recipeList.toList
      }
    }(databaseExecutionContext)
  }
}
