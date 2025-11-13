package co.uk.foodgen

import co.uk.foodgen.api.toResponseError
import zio.ZIO
import zio.json.DecoderOps
import zio.json.ast.Json

import scala.io.Source

object ResourceFileReader:
  private def readFile(fileName: String) =
    ZIO.acquireRelease(
      ZIO.attemptBlocking(Source.fromResource(fileName))
    )(bs => ZIO.succeed(bs.close()))

  def readJsonFile(fileName: String) = ZIO.scoped(
    readFile(fileName)
      .flatMap(contents =>
        ZIO
          .fromEither(
            contents
              .getLines()
              .toList
              .mkString(" ")
              .fromJson[Json]
          )
          .mapError(Exception(_))
      )
      .toResponseError
  )
end ResourceFileReader
