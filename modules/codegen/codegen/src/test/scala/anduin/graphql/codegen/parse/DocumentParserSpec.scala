// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.parse

import org.scalatest.{EitherValues, Matchers, WordSpec}

import anduin.graphql.codegen.TreePrinter
import anduin.graphql.test.TestDataSupport

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

final class DocumentParserSpec extends WordSpec with Matchers with EitherValues {

  private[this] def withTestData(testDataName: String)(
    run: (String => String => Unit) => Any
  ): Unit = {
    TestDataSupport.withTestData(testDataName) { (schema, readDocument) =>
      val parser = new DocumentParser(schema)

      s"""operate on test data "$testDataName"""" should {
        run { queryFilename =>
          { queryDebugString =>
            s"""parse queries in "$queryFilename" correctly""" in {
              val document = readDocument(queryFilename)

              val either = for {
                operations <- parser.parse(document)
              } yield {
                operations.map(TreePrinter.print).mkString("\n\n") should be(queryDebugString)
              }

              either.right.value
            }
          }
        }

        ()
      }
    }
  }

  "Document parser" when {

    withTestData("01-simplest-data") { validateQueries =>
      validateQueries("name-query.graphql")(
        """Query GetName
          |  data
          |    Query: name
          |      Name: firstName -> String
          |      Name: lastName -> Option of String
          |""".stripMargin
      )

      validateQueries("name-and-age-query.graphql")(
        """Query GetNameAndAge
          |  data
          |    Query: age -> Option of Int
          |    Query: name
          |      Name: firstName -> String
          |      Name: lastName -> Option of String
          |""".stripMargin
      )
    }
  }
}
