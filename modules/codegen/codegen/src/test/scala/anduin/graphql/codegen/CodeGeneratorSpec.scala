// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import org.scalatest.{EitherValues, Matchers, WordSpec}
import sangria.validation.QueryValidator

import anduin.graphql.codegen.parse.{DocumentParser, ParseState}
import anduin.graphql.test.TestDataSupport

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

final class CodeGeneratorSpec extends WordSpec with Matchers with EitherValues {

  "Code generator" should {

    "do something good" in {
      TestDataSupport.withTestData("10-simple-union-spreads") { (schema, readDocument) =>
        val parser = new DocumentParser(schema)
        val document = readDocument("hero-name-query.graphql")

        QueryValidator.default.validateQuery(schema, document).foreach { violation =>
          println(violation.errorMessage)
        }

        val either = for {
          result <- parser.parse(document).run(ParseState.Empty)
        } yield {
          val (_, operations) = result

          operations.foreach { operation =>
            println(CodeGenerator.generate(operation, Some("anduin.graphql.example")))
          }
        }

        either.right.value
      }
    }

    "do something better" in {
      TestDataSupport.withTestData("03-variables") { (schema, readDocument) =>
        val parser = new DocumentParser(schema)
        val document = readDocument("person-by-name-query.graphql")

        QueryValidator.default.validateQuery(schema, document).foreach { violation =>
          println(violation.errorMessage)
        }

        val either = for {
          result <- parser.parse(document).run(ParseState.Empty)
        } yield {
          val (state, operations) = result
          val inputTypeNames = state.inputNamedTypes.map(_.name)

          inputTypeNames should contain("NameInput")
          inputTypeNames should contain("FirstNameInput")

          operations.foreach { operation =>
            println(CodeGenerator.generate(operation, Some("anduin.graphql.example")))
          }
        }

        either.right.value
      }
    }
  }
}
