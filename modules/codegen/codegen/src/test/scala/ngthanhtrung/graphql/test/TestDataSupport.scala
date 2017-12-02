package ngthanhtrung.graphql.test

import scala.io.Source

import org.scalatest.EitherValues
import sangria.ast.Document
import sangria.parser.{DeliveryScheme, QueryParser}
import sangria.schema.Schema

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

private[graphql] final class TestDataSupport(testDataName: String) extends EitherValues {

  def readDocument(documentFilename: String): Document = {
    val either = for {
      documentSource <- Either.catchNonFatal {
        Source.fromResource(s"test-data/$testDataName/$documentFilename").mkString
      }

      document <- QueryParser.parse(documentSource)(DeliveryScheme.Either)
    } yield document

    either.right.value
  }

  def readSchema(): Schema[_, _] = {
    Schema.buildFromAst(readDocument("schema.graphql"))
  }
}

private[graphql] object TestDataSupport {

  def withTestData(testDataName: String)(
    run: (Schema[_, _], String => Document) => Any
  ): Unit = {
    val reader = new TestDataSupport(testDataName)
    val schema = reader.readSchema()
    run(schema, reader.readDocument)
    ()
  }
}
