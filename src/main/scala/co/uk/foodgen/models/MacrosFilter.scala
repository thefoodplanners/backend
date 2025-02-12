package co.uk.foodgen.models

case class MacrosFilter(
  maxCalories: Option[Int],
  maxCarbs: Option[Float],
  maxProteins: Option[Float],
  maxFats: Option[Float]
)

object MacrosFilter:
  def empty: MacrosFilter = MacrosFilter(None, None, None, None)
