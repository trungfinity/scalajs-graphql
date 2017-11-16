// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import scala.io.Source

import sangria.ast.Document
import sangria.parser.{DeliveryScheme, QueryParser}

import anduin.test.UnitSpec

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

final class DocumentParser2Spec extends UnitSpec {

  behavior of "Document parser"

  final class DataSetReader(name: String) {

    def readSchema(): Either[Throwable, Schema[_, _]] = {
      for {
        document <- readDocument("schema")
      } yield Schema.buildFromAst(document)
    }

    def readDocument(filename: String): Either[Throwable, Document] = {
      for {
        documentSource <- Either.catchNonFatal {
          Source.fromResource(s"anduin/graphql/codegen/$name/$filename.graphql").mkString
        }

        document <- QueryParser.parse(documentSource)(DeliveryScheme.Either)
      } yield document
    }
  }

  it should "run successsfully" in {
    val reader = new DataSetReader("01-simplest-data")
    implicit val sourceFile: Option[SourceFile] = None

    for {
      schema <- reader.readSchema()
      document <- reader.readDocument("name-query")

      documentParser = {
        val schemaTraversal = new SchemaTraversal(schema)
        val schemaLookup = new SchemaLookup(schema)
        new DocumentParser(schemaTraversal, schemaLookup)
      }

      operations <- documentParser.parse(document)
    } yield {
      operations.foreach { operation =>
        TreePrinter.print(operation) should be(
          """Query GetName
            |  data
            |    Query: name
            |      Name: firstName -> Option of String
            |      Name: lastName -> Option of String
            |""".stripMargin
        )
      }
    }
  }
}
