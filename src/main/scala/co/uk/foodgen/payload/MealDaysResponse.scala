package co.uk.foodgen.payload

import cats.syntax.eq.catsSyntaxEq
import co.uk.foodgen.service.models.MealView
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder, Json}
import io.scalaland.chimney.syntax.transformInto
import sttp.tapir.Schema

final case class MealDaysResponse(
  monday: Option[MealResponse],
  tuesday: Option[MealResponse],
  wednesday: Option[MealResponse],
  thursday: Option[MealResponse],
  friday: Option[MealResponse],
  saturday: Option[MealResponse],
  sunday: Option[MealResponse]
)

object MealDaysResponse:
  def apply(views: List[MealView], mealNumber: Int): MealDaysResponse =
    MealDaysResponse(
      monday = buildMealResponse(views, mealNumber, 1),
      tuesday = buildMealResponse(views, mealNumber, 2),
      wednesday = buildMealResponse(views, mealNumber, 3),
      thursday = buildMealResponse(views, mealNumber, 4),
      friday = buildMealResponse(views, mealNumber, 5),
      saturday = buildMealResponse(views, mealNumber, 6),
      sunday = buildMealResponse(views, mealNumber, 7)
    )

  private def buildMealResponse(views: List[MealView], mealNumber: Int, dayOfWeekValue: Int): Option[MealResponse] =
    views
      .find(mv => mv.date.getDayOfWeek.getValue === dayOfWeekValue && mv.mealNumber === mealNumber)
      .map(_.transformInto[MealResponse])

  given Codec[Option[MealResponse]] = Codec.from(
    Decoder.instance { cursor =>
      cursor.value.asObject match
        case Some(obj) if obj.isEmpty => Right(None)
        case _                        => cursor.as[MealResponse].map(Some(_))
    },
    Encoder.instance {
      case Some(mealResponse) => mealResponse.asJson
      case None               => Json.obj()
    }
  )
  given Codec[MealDaysResponse] = deriveCodec
  given Schema[MealDaysResponse] = Schema.derived
