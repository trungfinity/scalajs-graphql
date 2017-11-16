// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import scala.io.Source

import sangria.ast.Document
import sangria.parser.{DeliveryScheme, QueryParser}

import anduin.test.UnitSpec

// scalastyle:off underscore.import
import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

final class DocumentParserSpec extends UnitSpec {

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

  def printFields(fields: tree.Fields, indentation: Int, builder: StringBuilder): Unit = {
    fields.foreach {
      case (container, fieldsByContainer) =>
        fieldsByContainer.foreach { field =>
          builder.append(" " * indentation)
          builder.append(container.name)
          builder.append(": ")

          field match {
            case tree.CompositeField(name, subfields, _) =>
              builder.append(name)
              builder.append("\n")
              printFields(subfields, indentation + 2, builder)

            case tree.SimpleField(name, tpe) =>
              builder.append(name)
              builder.append(" -> ")
              builder.append(tpe.namedType.name)
              builder.append("\n")
          }
        }
    }
  }

  it should "run successsfully" in {
    val reader = new DataSetReader("01-simplest-data")

    for {
      schema <- reader.readSchema()
      document <- reader.readDocument("name-query")
      documentParser = {
        val sourceFile = None
        val schemaTraversal = new SchemaTraversal(schema, sourceFile)
        val schemaLookup = new SchemaLookup(schema, sourceFile)
        new DocumentParser(document, sourceFile, schemaTraversal, schemaLookup)
      }
      operations <- documentParser.parse()
    } yield {
      operations.foreach { operation =>
        val builder = new StringBuilder
        printFields(operation.underlying.fields, 0, builder)
        builder.result() should be(
          """Query: name
            |  Name: firstName -> String
            |  Name: lastName -> String
            |""".stripMargin
        )
      }
    }
  }
}
