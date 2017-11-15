// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import scala.concurrent.Future

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer
import sangria.validation.QueryValidator

// scalastyle:off underscore.import
import scala.meta.prettyprinters._

import cats.implicits._
import sangria.schema._
// scalastyle:on underscore.import

// scalastyle:off multiple.string.literals

object Test extends App {

  val characters = Fetcher.caching(
    (ctx: CharacterRepo, ids: Seq[String]) =>
      Future.successful(ids.flatMap(id => ctx.getHuman(id).orElse(ctx.getDroid(id))))
  )(HasId(_.id))

  val EpisodeEnum = EnumType(
    "Episode",
    Some("One of the films in the Star Wars Trilogy"),
    List(
      EnumValue("NEWHOPE", value = Episode.NEWHOPE, description = Some("Released in 1977.")),
      EnumValue("EMPIRE", value = Episode.EMPIRE, description = Some("Released in 1980.")),
      EnumValue("JEDI", value = Episode.JEDI, description = Some("Released in 1983."))
    )
  )

  val Character: InterfaceType[CharacterRepo, Character] =
    InterfaceType(
      "Character",
      "A character in the Star Wars Trilogy",
      () =>
        fields[CharacterRepo, Character](
          Field("id", StringType, Some("The id of the character."), resolve = _.value.id),
          Field(
            "name",
            OptionType(StringType),
            Some("The name of the character."),
            resolve = _.value.name
          ),
          Field(
            "friends",
            ListType(Character),
            Some("The friends of the character, or an empty list if they have none."),
            resolve = ctx => characters.deferSeqOpt(ctx.value.friends)
          ),
          Field(
            "appearsIn",
            OptionType(ListType(OptionType(EpisodeEnum))),
            Some("Which movies they appear in."),
            resolve = _.value.appearsIn.map(e => Some(e))
          )
      )
    )

  val Human =
    ObjectType(
      "Human",
      "A humanoid creature in the Star Wars universe.",
      interfaces[CharacterRepo, Human](Character),
      fields[CharacterRepo, Human](
        Field("id", StringType, Some("The id of the human."), resolve = _.value.id),
        Field(
          "name",
          OptionType(StringType),
          Some("The name of the human."),
          resolve = _.value.name
        ),
        Field(
          "friends",
          ListType(Character),
          Some("The friends of the human, or an empty list if they have none."),
          resolve = ctx => characters.deferSeqOpt(ctx.value.friends)
        ),
        Field(
          "appearsIn",
          OptionType(ListType(OptionType(EpisodeEnum))),
          Some("Which movies they appear in."),
          resolve = _.value.appearsIn.map(e => Some(e))
        ),
        Field(
          "homePlanet",
          OptionType(StringType),
          Some("The home planet of the human, or null if unknown."),
          resolve = _.value.homePlanet
        )
      )
    )

  val Droid = ObjectType(
    "Droid",
    "A mechanical creature in the Star Wars universe.",
    interfaces[CharacterRepo, Droid](Character),
    fields[CharacterRepo, Droid](
      Field(
        "id",
        StringType,
        Some("The id of the droid."),
        tags = ProjectionName("_id") :: Nil,
        resolve = _.value.id
      ),
      Field(
        "name",
        OptionType(StringType),
        Some("The name of the droid."),
        resolve = ctx => Future.successful(ctx.value.name)
      ),
      Field(
        "friends",
        ListType(Character),
        Some("The friends of the droid, or an empty list if they have none."),
        resolve = ctx => characters.deferSeqOpt(ctx.value.friends)
      ),
      Field(
        "appearsIn",
        OptionType(ListType(OptionType(EpisodeEnum))),
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn.map(e => Some(e))
      ),
      Field(
        "primaryFunction",
        OptionType(StringType),
        Some("The primary function of the droid."),
        resolve = _.value.primaryFunction
      )
    )
  )

  val ID = Argument("id", StringType, description = "id of the character")

  val EpisodeArg = Argument(
    "episode",
    OptionInputType(EpisodeEnum),
    description =
      "If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode."
  )

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

  private val query = ObjectType[CharacterRepo, Unit](
    name = "Query",
    fields = fields[CharacterRepo, Unit](
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
      ),
      Field(
        name = "hero",
        fieldType = Character,
        arguments = EpisodeArg :: Nil,
        deprecationReason = Some("Use `human` or `droid` fields instead"),
        resolve = (ctx) => ctx.ctx.getHero(ctx.arg(EpisodeArg))
      ),
      Field(
        name = "human",
        fieldType = OptionType(Human),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.getHuman(ctx.arg(ID))
      ),
      Field(
        "droid",
        Droid,
        arguments = ID :: Nil,
        resolve = Projector((ctx, f) => ctx.ctx.getDroid(ctx.arg(ID)).get)
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
        |
        |query union3 {
        |  union {
        |    ... on a1 {
        |      one
        |    }
        |    ... on a2 {
        |      two
        |    }
        |    ... on a3 {
        |      one
        |    }
        |  }
        |}
        |
        |query Now {
        |  hero {
        |    id
        |    ... on Human {
        |      homePlanet
        |    }
        |    ... on Droid {
        |      primaryFunction
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
            case tree.CompositeField(name, subfields, _) =>
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

  val sourceFile = Option.empty[File]
  val schemaTraversal = new SchemaTraversal(schema, sourceFile)
  val schemaLookup = new SchemaLookup(schema, sourceFile)
  val documentParser = new DocumentParser(document, sourceFile, schemaTraversal, schemaLookup)
  val documentTransformer = new DocumentTransformer(schema)
  val fieldTransformer = new FieldTransformer(sourceFile, schemaLookup)
  val generator = new Generator(sourceFile, schemaLookup, documentTransformer, fieldTransformer)

  for {
    operations <- documentParser.parse()
    _ <- operations.foldMapM[Result, Vector[String]] { operation =>
      for {
        transformedField <- fieldTransformer.transformField(operation.underlying)
        transformedOperation = operation.copy(underlying = transformedField)
        generatedObject <- generator.generate(transformedOperation)
      } yield {
        /* println(operation.name)
        printFields(operation.underlying.fields, 2)

        println() */

        println(transformedOperation.name)
        printFields(transformedOperation.underlying.fields, 2)

        println()

        val generatedSourceCode = generatedObject.syntax

        println(generatedSourceCode)
        println()
        println()

        Vector(generatedSourceCode)
      }
    }
  } yield ()
}
