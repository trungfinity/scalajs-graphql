// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.cli

import scala.io.Source
import scala.meta.prettyprinters.Syntax

import sangria.ast.Document
import sangria.parser.{DeliveryScheme, QueryParser}
import sangria.schema.Schema
import sangria.validation.QueryValidator

import anduin.graphql.codegen.{CodeGenerator, DocumentParser}

// scalastyle:off underscore.import
import caseapp._
import cats.implicits._
// scalastyle:on underscore.import

@AppName("GraphQL code generator")
@AppVersion("0.1.0")
@ProgName("codegen-cli")
final case class CodegenBeforeCommand()

sealed abstract class CodegenCommand extends Product with Serializable {
  def run(args: RemainingArgs): Unit
}

@CommandName("gen-ops")
final case class GenerateOperations(
  schema: String,
  `package`: Option[String]
) extends CodegenCommand {

  private[this] def readDocument(path: String): Document = {
    val either = for {
      documentSource <- Either.catchNonFatal {
        Source.fromFile(path).mkString
      }

      document <- QueryParser.parse(documentSource)(DeliveryScheme.Either)
    } yield document

    either.right.get
  }

  private[this] def readSchema(): Schema[_, _] = {
    Schema.buildFromAst(readDocument(schema))
  }

  def run(args: RemainingArgs): Unit = {
    val schema = readSchema()
    val parser = new DocumentParser(schema)

    args.args.foreach { documentPath =>
      val document = readDocument(documentPath)
      val violations = QueryValidator.default.validateQuery(schema, document)

      if (violations.nonEmpty) {
        violations.foreach { violation =>
          sys.error(violation.errorMessage)
        }

        sys.exit(1)
      }

      val operations = parser.parse(document).right.get

      operations.foreach { operation =>
        val output = CodeGenerator
          .generate(operation, `package`)
          .show[Syntax]

        println(output)
      }
    }
  }
}
