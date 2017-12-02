package ngthanhtrung.graphql.codegen.parse

import cats.kernel.Monoid
import sangria.schema.{EnumType, InputObjectType}

final case class ParseState(
  inputTypes: Set[Either[EnumType[_], InputObjectType[_]]]
)

object ParseState {

  val Empty: ParseState = ParseState(Set.empty)

  implicit val parseStateMonoid: Monoid[ParseState] = new Monoid[ParseState] {
    final def empty: ParseState = Empty
    final def combine(x: ParseState, y: ParseState) = {
      ParseState(x.inputTypes ++ y.inputTypes)
    }
  }
}
