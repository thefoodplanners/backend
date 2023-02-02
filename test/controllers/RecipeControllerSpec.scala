package controllers

import controllers.api.RecipeController
import models.{ FetchedMealSlot, RecipeDao }
import org.joda.time.LocalDate
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{ GET, contentAsString, defaultAwaitTimeout }
import play.api.test.{ FakeRequest, Helpers }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class RecipeControllerSpec extends PlaySpec with MockitoSugar {
  "RecipeController#mealSlotToArray" should {
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    "return the correct 2d array" in {

      val fetchedMealSlots = List(
        FetchedMealSlot(LocalDate.parse("2023-01-30").toDate, 1, 5),
        FetchedMealSlot(LocalDate.parse("2023-01-30").toDate, 2, 3)
      )

      val database = mock[RecipeDao]
      when(database.fetchAllMealSlots("id123", "2023-W02"))
        .thenReturn(Future.successful(fetchedMealSlots -> 3))

      val controller = new RecipeController(Helpers.stubControllerComponents(), database)

      val request = FakeRequest(GET, "/allMeals")
        .withSession("USERID" -> "id123")

      val result = controller.getAllMealSlots("2023-W02").apply(request)
      val bodyText = contentAsString(result)
      bodyText mustBe """[["5","","","","","",""],["3","","","","","",""],["","","","","","",""]]"""
    }
  }
}
