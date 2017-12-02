package ngthanhtrung.graphql.example

import scala.scalajs.js

import ngthanhtrung.scalajs.noton.{Decoder, Encoder}

object Main {

  def testSomeoneQuery(): Unit = {
    val firstNameJs = js.Dynamic.literal("value" -> "Trung")
    val nameJs = js.Dynamic.literal(
      "firstName" -> firstNameJs,
      "lastName" -> null
    )

    val firstName = GetSomeoneQuery.Data.Someone.Name.FirstName("Trung")(firstNameJs)
    val name = GetSomeoneQuery.Data.Someone.Name(firstName, None)(nameJs)
    val decodedName = Decoder[GetSomeoneQuery.Data.Someone.Name].apply(nameJs)

    println("Comparing between:")
    println(s"1. Original name: $name")
    println(s"2. Decoded name: $decodedName")

    assert(decodedName == Right(name))

    println("Matched!")
  }

  def testPersonQuery(): Unit = {
    val firstNameJs = js.Dynamic.literal("value" -> "Trung")
    val nameJs = js.Dynamic.literal(
      "firstName" -> firstNameJs,
      "lastName" -> null
    )

    val firstName = GetPersonQuery.Data.Person.Name.FirstName("Trung")(firstNameJs)
    val name = GetPersonQuery.Data.Person.Name(firstName, None)(nameJs)
    val decodedName = Decoder[GetPersonQuery.Data.Person.Name].apply(nameJs)

    println("Comparing between:")
    println(s"1. Original name: $name")
    println(s"2. Decoded name: $decodedName")

    assert(decodedName == Right(name))

    println("Matched!")

    val variablesJs = js.Dynamic.literal("id" -> "13579")
    val variables = GetPersonQuery.Variables("13579")
    val encodedVariables = Encoder[GetPersonQuery.Variables].apply(variables)

    println("Comparing between:")
    println(s"1. Original variables: ${js.JSON.stringify(variablesJs)}")
    println(s"2. Encoded variables: ${js.JSON.stringify(encodedVariables)}")

    assert(js.JSON.stringify(variablesJs) == js.JSON.stringify(encodedVariables))

    println("Matched")
  }

  def testPersonByNameQuery(): Unit = {
    val firstNameInputJs = js.Dynamic.literal("value" -> "Trung")
    val nameInputJs = js.Dynamic.literal(
      "firstName" -> firstNameInputJs,
      "lastName" -> "Nguyen"
    )

    val variablesJs = js.Dynamic.literal("name" -> nameInputJs)

    val firstNameInput = FirstNameInput("Trung")
    val nameInput = NameInput(firstNameInput, Some("Nguyen"))
    val variables = GetPersonByNameQuery.Variables(nameInput)
    val encodedVariables = Encoder[GetPersonByNameQuery.Variables].apply(variables)

    println("Comparing between:")
    println(s"1. Original variables: ${js.JSON.stringify(variablesJs)}")
    println(s"2. Encoded variables: ${js.JSON.stringify(encodedVariables)}")

    assert(js.JSON.stringify(variablesJs) == js.JSON.stringify(encodedVariables))

    println("Matched")
  }

  def testPeopleByGenderQuery(): Unit = {
    val genderJs = "MALE"
    val gender = Gender.MALE
    val encodedGender = Encoder[Gender].apply(gender)
    val decodedGender = Decoder[Gender].apply(genderJs)

    println("Comparing between:")
    println(s"1. Original gender: ${js.JSON.stringify(genderJs)}")
    println(s"2. Encoded gender: ${js.JSON.stringify(encodedGender)}")

    assert(js.JSON.stringify(genderJs) == js.JSON.stringify(encodedGender))

    println("Matched")

    println("Comparing between:")
    println(s"1. Original gender: $gender")
    println(s"2. Decoded gender: $decodedGender")

    assert(decodedGender == Right(gender))

    println("Matched")

    val variablesJs = js.Dynamic.literal("gender" -> genderJs)
    val variables = GetPeopleByGenderQuery.Variables(gender)
    val encodedVariables = Encoder[GetPeopleByGenderQuery.Variables].apply(variables)

    println("Comparing between:")
    println(s"1. Original variables: ${js.JSON.stringify(variablesJs)}")
    println(s"2. Encoded variables: ${js.JSON.stringify(encodedVariables)}")

    assert(js.JSON.stringify(variablesJs) == js.JSON.stringify(encodedVariables))

    println("Matched")
  }

  def main(args: Array[String]): Unit = {
    testSomeoneQuery()
    testPersonQuery()
    testPersonByNameQuery()
    testPeopleByGenderQuery()
  }
}
