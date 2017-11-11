// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import sangria.parser.QueryParser
import sangria.validation.QueryValidator

// scalastyle:off underscore.import
import sangria.schema._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

object Test extends App {

  private val StringArgument = Argument(
    name = "string",
    argumentType = StringType
  )

  private val a1 = ObjectType[Unit, Unit](
    name = "a1",
    fields = fields[Unit, Unit](
      Field(
        name = "one",
        fieldType = StringType,
        resolve = _ => "1"
      ),
      Field(
        name = "two",
        fieldType = IntType,
        resolve = _ => 2
      )
    )
  )

  private val a2 = ObjectType[Unit, Unit](
    name = "a2",
    fields = fields[Unit, Unit](
      Field(
        name = "one",
        fieldType = StringType,
        resolve = _ => "1"
      ),
      Field(
        name = "two",
        fieldType = FloatType,
        resolve = _ => 2.2
      )
    )
  )

  private val a3 = ObjectType[Unit, Unit](
    name = "a3",
    fields = fields[Unit, Unit](
      Field(
        name = "one",
        fieldType = StringType,
        resolve = _ => "1"
      )
    )
  )

  private val query = ObjectType[Unit, Unit](
    name = "Query",
    fields = fields[Unit, Unit](
      Field(
        name = "foo",
        fieldType = StringType,
        resolve = _ => "foo"
      ),
      Field(
        name = "bar",
        fieldType = ObjectType[Unit, Unit](
          name = "Bar",
          fields = fields[Unit, Unit](
            Field(
              name = "bar1",
              fieldType = OptionType(StringType),
              resolve = _ => "bar1"
            ),
            Field(
              name = "bar2",
              fieldType = IntType,
              resolve = _ => 8 // scalastyle:ignore magic.number
            )
          )
        ),
        resolve = _ => ()
      ),
      Field(
        name = "quz",
        fieldType = OptionType(IntType),
        resolve = _ => 10 // scalastyle:ignore magic.number
      ),
      Field(
        name = "params",
        fieldType = ObjectType[Unit, String](
          name = "Params",
          fields = fields[Unit, String](
            Field(
              name = "what",
              fieldType = StringType,
              resolve = _.value
            )
          )
        ),
        arguments = List(StringArgument),
        resolve = { ctx =>
          ctx.arg[String](StringArgument)
        }
      ),
      Field(
        name = "union",
        fieldType = UnionType(
          name = "union",
          types = List(a1, a2, a3)
        ),
        resolve = _ => ()
      ),
      Field(
        name = "union2",
        fieldType = UnionType(
          name = "union2",
          types = List(a1)
        ),
        resolve = _ => ()
      )
    )
  )

  private val schema = Schema(query)

  private val document = QueryParser
    .parse(
      """query Foo {
        |  ...Query
        |}
        |
        |query Bar {
        |  ...FooBar
        |  bar {
        |    bar1
        |    bar2
        |  }
        |}
        |
        |query FooAndBar {
        |  ...FooBar
        |  ... {
        |    foo
        |  }
        |  ... on Query {
        |    foo
        |  }
        |  ... on Query {
        |    ...FooBar
        |  }
        |}
        |
        |fragment FooBar on Query {
        |  foo
        |  bar {
        |    bar1
        |  }
        |}
        |
        |fragment Query on Query {
        |  foo
        |}
        |
        |query Quz {
        |  quz
        |}
        |
        |query WithVars($var: String!) {
        |  params(string: $var) {
        |    what
        |  }
        |}
        |
        |query union {
        |  union {
        |    ... on union {
        |      ... on union {
        |        ... on a1 {
        |          one
        |        }
        |      }
        |    }
        |    ... on union2 {
        |      ... on a1 {
        |        one
        |      }
        |    }
        |  }
        |}
        |
        |query union2 {
        |  union2 {
        |    ... on union {
        |      ... on a2 {
        |        one
        |      }
        |    }
        |  }
        |}
    """.stripMargin
    )
    .get

  val violations = QueryValidator.default.validateQuery(schema, document)

  if (violations.nonEmpty) {
    violations.foreach { violation =>
      println(violation.errorMessage)
    }

    sys.exit(1)
  }

  def printFields(fields: tree.Fields, indentation: Int): Unit = {
    fields.foreach {
      case (container, fieldsByContainer) =>
        fieldsByContainer.foreach { field =>
          print(" " * indentation)
          print(container.name)
          print(": ")

          field match {
            case tree.CompositeField(name, subfields) =>
              println(name)
              printFields(subfields, indentation + 2)

            case tree.SimpleField(name, tpe) =>
              print(name)
              print(" -> ")
              println(tpe.namedType.name)
          }
        }
    }
  }

  new Processor(schema, document)
    .parse()
    .getOrElse(throw new RuntimeException("This should not happen."))
    .foreach { operation =>
      println(operation.name)
      printFields(operation.tpe.fields, 2)
    }
}
