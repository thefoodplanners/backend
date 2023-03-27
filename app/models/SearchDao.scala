package models

import anorm._
import org.joda.time.LocalDate
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future
import scala.language.postfixOps

class SearchDao @Inject()(
  db: Database,
  calendarDao: CalendarDao
)
  (databaseExecutionContext: DatabaseExecutionContext) {

  def searchForRecipes(query: String): Future[Seq[Recipe]] = {
    Future {
      db.withConnection { implicit conn =>
        val queryWithWildcard: String = s"%$query%"
        SQL"""
             SELECT * FROM Recipe
             WHERE Name LIKE $queryWithWildcard;
             """.as(calendarDao.recipeParser.*)
      }
    }(databaseExecutionContext)
  }

}
