package co.uk.foodgen.service.models

import sttp.tapir.{Codec, CodecFormat}

enum Period:
  case Day, Week, Month, Year

  override def toString: String = productPrefix.toLowerCase

object Period:
  given Codec[String, Period, CodecFormat.TextPlain] = Codec.derivedEnumeration.defaultStringBased
