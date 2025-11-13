package co.uk.foodgen

import zio.http.codec.CodecConfig
import zio.schema.NameFormat
import zio.schema.codec.JsonCodec
import zio.{ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    CodecConfig.configLayer(
      CodecConfig(
        explicitEmptyCollections = JsonCodec.ExplicitConfig(encoding = true, decoding = true),
        explicitNulls = JsonCodec.ExplicitConfig(encoding = false, decoding = false),
        fieldNameFormat = NameFormat.CamelCase
      )
    )

  def run = ServerConfig.server
