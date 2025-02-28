package co.uk.foodgen

import cats.effect.IO
import fs2.io.readInputStream
import fs2.text
import io.circe.{Json, parser}

object ResourceFileReader:

  def readFile(fileName: String): IO[String] =
    val stream = IO.blocking(Option(getClass.getClassLoader.getResourceAsStream(fileName)).get)
    readInputStream(stream, 4096)
      .through(text.utf8.decode)
      .compile
      .string

  def readJsonFile(fileName: String): IO[Json] =
    readFile(fileName).map(parser.parse(_).fold(fail => throw fail.underlying, identity))

end ResourceFileReader
