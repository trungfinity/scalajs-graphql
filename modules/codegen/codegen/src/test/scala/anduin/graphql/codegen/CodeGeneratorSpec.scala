// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import org.scalatest.{EitherValues, WordSpec}
import sangria.validation.QueryValidator

import anduin.graphql.codegen.parse.DocumentParser
import anduin.graphql.test.TestDataSupport

final class CodeGeneratorSpec extends WordSpec with EitherValues {

  "Code generator" should {

    "do something good" in {
      TestDataSupport.withTestData("10-simple-union-spreads") { (schema, readDocument) =>
        val parser = new DocumentParser(schema)
        val document = readDocument("hero-name-query.graphql")

        QueryValidator.default.validateQuery(schema, document).foreach { violation =>
          println(violation.errorMessage)
        }

        val either = for {
          operations <- parser.parse(document)
        } yield {
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
          operations <- parser.parse(document)
        } yield {
          operations.foreach { operation =>
            println(CodeGenerator.generate(operation, Some("anduin.graphql.example")))
          }
        }

        either.right.value
      }
    }
  }
}
