// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.example

import scala.scalajs.js

object Main {

  def main(args: Array[String]): Unit = {
    val raw = js.Dynamic.literal(
      "firstName" -> "Trung",
      "lastName" -> null
    )

    val name = GetNameQuery.Data.Name("Trung", None)(raw)
    val decodedName = GetNameQuery.Data.Name.decoder(raw)

    println("Comparing between:")
    println(s"1. Original name: $name")
    println(s"2. Decoded name: $decodedName")

    assert(decodedName == Right(name))

    println("Matched!")
  }
}
