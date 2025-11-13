package co.uk.foodgen

import co.uk.foodgen.domain.Entity.*
import io.getquill.context.ZioJdbc.QIO
import io.getquill.{MappedEncoding, NamingStrategy, PluralizedTableNames, PostgresZioJdbcContext, SnakeCase}
import zio.ZIO

import java.sql.SQLException
import scala.annotation.targetName

package object repositories:
  object DbContext extends PostgresZioJdbcContext(NamingStrategy(SnakeCase, PluralizedTableNames))

  given [A]: MappedEncoding[EntityId[A], Long] = MappedEncoding(_.value)
  given [A]: MappedEncoding[Long, EntityId[A]] = MappedEncoding(EntityId.apply)

  extension [A](dbio: QIO[List[A]])
    @targetName("expectOneList")
    def expectOne: QIO[A] =
      dbio.flatMap {
        case single :: Nil => ZIO.succeed(single)
        case Nil           => ZIO.fail(new SQLException("Expected exactly one row, got 0"))
        case many          => ZIO.fail(new SQLException(s"Expected exactly one row, got ${many.size}"))
      }
    def expectAtLeastOne: QIO[A] =
      dbio.flatMap {
        case head :: _ => ZIO.succeed(head)
        case Nil       => ZIO.fail(new SQLException("Expected at least one row, got 0"))
      }

  extension [A](dbio: QIO[Option[A]])
    @targetName("expectOneOption")
    def expectOne: QIO[A] =
      dbio.flatMap {
        case Some(value) => ZIO.succeed(value)
        case None        => ZIO.fail(new SQLException("Expected exactly one row, got 0"))
      }

end repositories
