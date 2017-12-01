// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.scalajs.noton.generic

import scala.reflect.macros.blackbox
import scala.scalajs.js

import anduin.scalajs.noton.{Decoder, Encoder}

final class DerivationMacros(val c: blackbox.Context) {

  import c.universe._ // scalastyle:ignore import.grouping underscore.import

  private[this] val jsAnyType = typeOf[js.Any]

  private[this] case class Member(name: TermName, decodedName: String, tpe: Type)
  private[this] case class RawMember(name: TermName, decodedName: String)

  private[this] def membersFromParamList(paramList: List[Symbol], tpe: Type): List[Member] = {
    paramList.map { param =>
      Member(
        param.name.toTermName,
        param.name.decodedName.toString,
        tpe.decl(param.name).typeSignature.resultType.asSeenFrom(tpe, tpe.typeSymbol)
      )
    }
  }

  private[this] def membersFromPrimaryCtor(tpe: Type): Option[(List[Member], RawMember)] = {
    def fail(): Nothing = {
      c.abort(
        c.enclosingPosition,
        "expect the second parameter list to contain exactly one scala.scalajs.js.Any member"
      )
    }

    tpe.decls.collectFirst {
      case primaryCtor: MethodSymbol if primaryCtor.isPrimaryConstructor =>
        val paramLists = primaryCtor.paramLists

        if (paramLists.size <= 1 || paramLists.tail.head.size <= 0) {
          fail()
        }

        val members = membersFromParamList(paramLists.head, tpe)
        val rawMember = membersFromParamList(paramLists.tail.head, tpe).head

        if (!(rawMember.tpe =:= jsAnyType)) {
          fail()
        }

        (members, RawMember(rawMember.name, rawMember.decodedName))
    }
  }

  private[this] val encoderTypeCtor: Type = typeOf[Encoder[_]].typeConstructor
  private[this] val decoderTypeCtor: Type = typeOf[Decoder[_]].typeConstructor

  private[this] case class Instance(typeCtor: Type, tpe: Type, name: TermName) {

    def resolve(): Tree = c.inferImplicitValue(appliedType(typeCtor, List(tpe))) match {
      case EmptyTree => c.abort(c.enclosingPosition, s"could not find implicit $typeCtor[$tpe]")
      case instance: Tree => instance
    }
  }

  private[this] case class Instances(encoder: Instance, decoder: Instance)

  private[this] case class ProductRepr(members: List[Member]) {

    lazy val instances: List[Instances] = {
      members.reverse.foldLeft(List.empty[Instances]) {
        case (accumulated, Member(_, _, tpe)) if !accumulated.exists(_.encoder.tpe =:= tpe) =>
          val instances = Instances(
            Instance(encoderTypeCtor, tpe, TermName(c.freshName("encoder"))),
            Instance(decoderTypeCtor, tpe, TermName(c.freshName("decoder")))
          )

          instances :: accumulated
      }
    }

    private[this] def fail(tpe: Type): Nothing = {
      c.abort(c.enclosingPosition, s"invalid member type $tpe")
    }

    def encoder(tpe: Type): Instance = {
      instances.map(_.encoder).find(_.tpe =:= tpe).getOrElse(fail(tpe))
    }

    def decoder(tpe: Type): Instance = {
      instances.map(_.decoder).find(_.tpe =:= tpe).getOrElse(fail(tpe))
    }
  }

  def encoder[A: c.WeakTypeTag]: c.Expr[Encoder[A]] = {
    val tpe = weakTypeOf[A]

    membersFromPrimaryCtor(tpe).fold(
      c.abort(c.enclosingPosition, s"could not find the primary constructor of $tpe")
    ) {
      case (members, _) =>
        val repr = ProductRepr(members)

        val instanceDefs = repr.instances.map(_.encoder).map {
          case instance @ Instance(_, instanceType, instanceName) =>
            val resolved = instance.resolve()
            q"val $instanceName: _root_.anduin.scalajs.noton.Encoder[$instanceType] = $resolved"
        }

        val params = repr.members.map {
          case Member(memberName, memberDecodedName, memberType) =>
            q"""
              _root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.scala.scalajs.js.Any](
                ${Literal(Constant(memberDecodedName))},
                ${repr.encoder(memberType).name}(a.$memberName)
              )
            """
        }

        c.Expr[Encoder[A]](
          q"""
            _root_.anduin.scalajs.noton.Encoder.instance { a =>
              ..$instanceDefs
              _root_.scala.scalajs.js.Dynamic.literal(..$params)
            }
          """
        )
    }
  }

  def decoder[A: c.WeakTypeTag]: c.Expr[Decoder[A]] = { // scalastyle:ignore method.length
    val tpe = weakTypeOf[A]

    membersFromPrimaryCtor(tpe).fold(
      c.abort(c.enclosingPosition, s"could not find the primary constructor of $tpe")
    ) {
      case (members, _) =>
        val repr = ProductRepr(members)

        val instanceDefs = repr.instances.map(_.decoder).map {
          case instance @ Instance(_, instanceType, instanceName) =>
            val resolved = instance.resolve()
            q"val $instanceName: _root_.anduin.scalajs.noton.Decoder[$instanceType] = $resolved"
        }

        val membersWithNames = repr.members.map { member =>
          (member, TermName(c.freshName("member")))
        }

        val body = membersWithNames.reverse.foldLeft[Tree](
          q"""
            new _root_.scala.Right[_root_.java.lang.Throwable, $tpe](
              new $tpe(..${membersWithNames.map(_._2)})(any)
            ): _root_.anduin.scalajs.noton.Decoder.Result[$tpe]
          """
        ) {
          case (accumulated, (Member(_, memberDecodedName, memberType), memberResultName)) =>
            val resultName = TermName(c.freshName("result"))

            q"""
              val $resultName: _root_.anduin.scalajs.noton.Decoder.Result[$memberType] =
                ${repr.decoder(memberType).name}(dynamic.selectDynamic($memberDecodedName))

              if ($resultName.isRight) {
                val $memberResultName: $memberType = $resultName
                  .asInstanceOf[_root_.scala.Right[_root_.java.lang.Throwable, $memberType]]
                  .value
                $accumulated
              } else {
                $resultName.asInstanceOf[_root_.anduin.scalajs.noton.Decoder.Result[$tpe]]
              }
            """
        }

        c.Expr[Decoder[A]](
          q"""
            _root_.anduin.scalajs.noton.Decoder.instance { any =>
              val dynamic = any.asInstanceOf[_root_.scala.scalajs.js.Dynamic]
              ..$instanceDefs
              $body
            }
          """
        )
    }
  }
}
