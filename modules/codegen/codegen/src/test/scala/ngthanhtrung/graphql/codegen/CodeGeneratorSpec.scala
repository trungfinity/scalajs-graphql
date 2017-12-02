package ngthanhtrung.graphql.codegen

import org.scalatest.{EitherValues, Matchers, WordSpec}
import sangria.validation.QueryValidator

import ngthanhtrung.graphql.codegen.parse.{DocumentParser, ParseState}
import ngthanhtrung.graphql.test.TestDataSupport

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

final class CodeGeneratorSpec extends WordSpec with Matchers with EitherValues {

  private[this] val packageName = Some("ngthanhtrung.graphql.example")

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
            println(CodeGenerator.generateOperation(operation, packageName))
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
          val inputTypeNames = state.inputTypes.map {
            case Left(enumType) => enumType.name
            case Right(inputObjectType) => inputObjectType.name
          }

          inputTypeNames should contain("NameInput")
          inputTypeNames should contain("FirstNameInput")

          state.inputTypes.foreach {
            case Left(enumType) =>
              println(CodeGenerator.generateEnumType(enumType, packageName))

            case Right(inputObjectType) =>
              println(CodeGenerator.generateInputObjectType(inputObjectType, packageName))
          }

          operations.foreach { operation =>
            println(CodeGenerator.generateOperation(operation, packageName))
          }
        }

        either.right.value
      }
    }
  }
}
