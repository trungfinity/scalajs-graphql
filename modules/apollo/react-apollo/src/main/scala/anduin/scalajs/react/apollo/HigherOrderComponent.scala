// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.react.apollo

import scala.language.higherKinds
import scala.scalajs.js

import japgolly.scalajs.react.{Children => ChildrenType}
import japgolly.scalajs.react.internal.Box

import anduin.scalajs.noton.{Decoder, Encoder}

// scalastyle:off underscore.import
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
// scalastyle:on underscore.import

final class HigherOrderComponent[Props, ChildProps](
  raw: internal.HigherOrderComponent[js.Object, js.Object]
)(
  implicit encoder: Encoder[Props],
  decoder: Decoder[ChildProps]
) {

  def apply[Ctor[-p, +u] <: CtorType[p, u], Children <: ChildrenType](
    component: ScalaComponent[ChildProps, _, _, Ctor]
  )(
    implicit childCtor: CtorType.Summoner.Aux[Box[ChildProps], Children, Ctor],
    ctor: CtorType.Summoner[js.Object, Children]
  ): JsComponent.ComponentWithRoot[ // scalastyle:ignore no.whitespace.after.left.bracket
    Props,
    ctor.CT,
    JsComponent.Unmounted[js.Object, Null],
    js.Object,
    ctor.CT,
    JsComponent.Unmounted[js.Object, Null]
  ] = {
    val render = { raw: js.Any =>
      decoder(raw) match {
        case Right(childProps) =>
          val children = PropsChildren
            .fromRawProps(raw.asInstanceOf[js.Object]) // scalastyle:ignore token
            .toList
            .map(VdomNode.apply)
          component.ctor.applyGeneric(childProps)(children: _*).raw

        case Left(throwable) =>
          <.span(throwable.getMessage)
      }
    }

    JsComponent[js.Object, Children, Null](raw(render)).cmapCtorProps { props: Props =>
      encoder(props).asInstanceOf[js.Object] // scalastyle:ignore token
    }
  }
}
