// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen2

import org.scalatest.{EitherValues, WordSpec}
import sangria.validation.QueryValidator

import anduin.graphql.test.TestDataSupport

final class CodeGeneratorSpec extends WordSpec with EitherValues {

  "Code generator" should {

    "do something good" in {
      TestDataSupport.withTestData("10-simple-union-spreads") { (schema, readDocument) =>
        val parser = new DocumentParser(schema)
        val document = readDocument("hero-name-query.graphql")
        val generator = new CodeGenerator(Some("anduin.graphql.example"))

        QueryValidator.default.validateQuery(schema, document).foreach { violation =>
          println(violation.errorMessage)
        }

        val either = for {
          operations <- parser.parse(document)
        } yield {
          operations.foreach { operation =>
            println(generator.generate(operation))
          }
        }

        either.right.value
      }
    }
  }
}
