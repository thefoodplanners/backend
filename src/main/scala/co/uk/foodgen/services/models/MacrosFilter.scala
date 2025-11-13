package co.uk.foodgen.services.models

final case class MacrosFilter(
  calories: Option[Int],
  carbohydrates: Option[Float],
  proteins: Option[Float],
  fats: Option[Float]
)

object MacrosFilter:
  def empty: MacrosFilter = MacrosFilter(None, None, None, None)
