package ngthanhtrung.scalajs.react.apollo

import scala.language.higherKinds
import scala.scalajs.js

import ngthanhtrung.scalajs.noton.{Decoder, Encoder}

object ReactApollo {

  def graphql[Query <: ApolloQuery](
    query: Query
  )(
    implicit encoder: Encoder[Query#Variables],
    decoder: Decoder[Query#Data]
  ): HigherOrderComponent[Query#Variables, ApolloQueryProps[Query#Data]] = {
    // scalastyle:off token
    new HigherOrderComponent[Query#Variables, ApolloQueryProps[Query#Data]](
      internal.ReactApollo
        .graphql(query.raw)
        .asInstanceOf[internal.HigherOrderComponent[js.Object, js.Object]]
    )
    // scalastyle:on token
  }
}
