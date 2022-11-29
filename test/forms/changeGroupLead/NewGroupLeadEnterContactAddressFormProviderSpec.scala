/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.changeGroupLead

import forms.behaviours.StringFieldBehaviours
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider._
import org.scalacheck.Gen
import play.api.data.FormError


class NewGroupLeadEnterContactAddressFormProviderSpec extends StringFieldBehaviours {


  val allowChars = ('A' to 'Z').toList ++
    ('a' to 'z').toList ++
    ('0' to '9').toList ++
    Seq('&', '-', '`', ''', ',', '.')

  val notAllowedChar = ('!' to '~').mkString.filter(o => !allowChars.contains(o))

  val form = new NewGroupLeadEnterContactAddressFormProvider()()
  private val maxLengthKey = (n: Int) => s"newGroupLeadEnterContactAddress.error.addressLine${n}.length"
  private val invalidKey = (n: Int) => s"newGroupLeadEnterContactAddress.error.addressLine${n}.invalid.line"
  private val maxLength = 35

  case class StrRange(min: Int = 1, max: Int)

  val table = Table(
    ("description", "fieldName", "maxStringLength", "requiresKey", "invalidKey", "maxLengthKey", "optionalOrMandatory"),
    ("addressLine1 field", "addressLine1", maxLength, addressLine1RequiredKey, invalidKey(1), maxLengthKey(1), "mandatory"),
    ("addressLine2 field", "addressLine2", maxLength, "Not required", invalidKey(2), maxLengthKey(2), "optional"),
    ("addressLine3 field", "addressLine3", maxLength, "Not required", invalidKey(3), maxLengthKey(3), "optional"),
    ("addressLine4 field", "addressLine4", maxLength, addressLine4RequiredKey, invalidKey(4), maxLengthKey(4), "mandatory")
  )

  forAll(table) {
    (
      description: String,
      fieldName: String,
      maxStringLength: Int,
      requiresKey: String,
      invalidKey: String,
      maxLengthKey: String,
      optionalOrMandatory: String
    ) =>

      s".$fieldName" - {
        behave like fieldThatBindsValidData(
          form,
          fieldName,
          stringsWithMaxLength(maxStringLength, 1, Some(Gen.alphaNumChar))
        )

        if(optionalOrMandatory.equals("mandatory")) {
          behave like mandatoryField(
            form,
            fieldName,
            requiredError = FormError(fieldName, requiresKey))

          "not bind white space only values" in {

            val result = form.bind(Map(fieldName -> " \t")).apply(fieldName)
            result.errors mustEqual Seq(FormError(fieldName, requiresKey))
          }
        }

        if(optionalOrMandatory.equals("optional")) {
          s"$description does not error when binding emptyString" in {
            val result = form.bind(emptyForm).apply(fieldName)

            result.errors mustEqual Seq()
            result.value mustBe None
          }

          s"$description does not error when binding blank values" in {
            val result = form.bind(Map(fieldName -> "")).apply(fieldName)

            result.errors mustEqual Seq()
            result.value mustBe Some("") //this isnt right, it should be None, but the dodgy apply doesn't do the transformations
          }

          s"$description does not error when binding white space only values" in {
            val result = form.bind(Map(fieldName -> " \t")).apply(fieldName)

            result.value mustBe Some(" \t") //this isnt right, it should be None, but the dodgy apply doesn't do the transformations
          }
        }

        s"$description must have maximum length of ${maxStringLength}" in {
          val result = form.bind(Map(fieldName -> List.fill(maxStringLength + 1)("b").mkString)).apply(fieldName)

          result.errors.head.key mustEqual fieldName
          result.errors.head.message mustEqual maxLengthKey
        }

        s"$description can include space" in {
          val result = form.bind(Map(fieldName -> "address line")).apply(fieldName)

          result.value.value mustBe "address line"
          result.errors mustBe empty
        }

        notAllowedChar.foreach(o => {

          s"$description must not include '$o' character" in {
            val result = form.bind(Map(fieldName -> s"gh${o} Nmh")).apply(fieldName)

            result.errors.head.key mustEqual fieldName
            result.errors.head.message mustEqual invalidKey
          }
        })

        s"$description must not include foreign character" in {
            val result = form.bind(Map(fieldName -> "街道")).apply(fieldName)
            result.value.value mustBe "街道"
            result.errors.head.message mustEqual invalidKey
        }

        s"$description must not include unicode character" in {
          (161 to 255).map(_.toChar).foreach(o => {
            val result = form.bind(Map(fieldName -> s"Ny${o} JKe")).apply(fieldName)

            assertResult(result.errors.head.key, s"'${o}' should be an valid character")(fieldName)
            result.errors.head.message mustEqual invalidKey
          })
        }
      }
  }

  ".postalCode" - {
    val fieldName = "postalCode"

    "when is mandatory (countryCode is GB)" - {
      "must bind valid data" in {
        val result = form.bind(Map(countryCode -> "GB", fieldName -> "NE4 7FG")).apply(fieldName)

        result.value.value mustBe "NE4 7FG"
        result.errors mustBe empty
      }

      "must not error and bind valid data with extra spaces" in {
        val result = form.bind(Map(countryCode -> "GB", fieldName -> " NE4 7FG")).apply(fieldName)

        result.errors mustBe empty
        result.value.value mustBe " NE4 7FG" //this isnt right, it should be "NE4 7FG", but the dodgy apply doesn't do the transformations
      }

      "must not error and bind valid data with lower case" in {
        val result = form.bind(Map(countryCode -> "GB", fieldName -> "ne4 7fg")).apply(fieldName)

        result.errors mustBe empty
        result.value.value mustBe "ne4 7fg" //this isnt right, it should be "NE4 7FG", but the dodgy apply doesn't do the transformations
      }

      "must error when empty" in {
        val result = form.bind(Map(countryCode -> "GB")).apply(fieldName)

        result.errors.head.key mustEqual fieldName
        result.errors.head.message mustEqual postalCodeRequiredKey
      }

      "must error when binding blank values" in {
        val result = form.bind(Map(fieldName -> "", countryCode -> "GB")).apply(fieldName)

        result.errors.head.key mustEqual fieldName
        result.errors.head.message mustEqual postalCodeRequiredKey
      }


      val postalCodeTable = Table(
        ("description", "postalCode"),
        ("must have maximum length of 8", "ND4 6TYH"),
        ("must have minimum length of 5", "N5 1N"),
        ("must not be a whole number", "123456")
      )

      forAll(postalCodeTable) {
        (
          description: String,
          postalCode: String
        ) =>

          description in {
            val result = form.bind(Map(fieldName -> postalCode, countryCode -> "GB"))
              .apply(fieldName)

            result.errors.head.key mustEqual fieldName
            result.errors.head.message mustEqual postalCodeMaxLengthKey
          }
      }

      notAllowedChar.foreach(o => {

        s"must not include '$o' character" in {
          val result = form.bind(Map(fieldName -> s"N${o}5 6NM", countryCode -> "GB"))
            .apply(fieldName)

          result.errors.head.key mustEqual fieldName
          result.errors.head.message mustEqual postalCodeMaxLengthKey
        }
      })

      s"must not include special character" in {
        val result = form.bind(Map(fieldName -> s"N${notAllowedChar.head}5 6NM", "countryCode" -> "GB"))
          .apply(fieldName)

        result.errors.head.key mustEqual fieldName
        result.errors.head.message mustEqual postalCodeMaxLengthKey
      }

      s"must not include foreign character" in {
        val result = form.bind(Map(fieldName -> "街道", countryCode -> "GB")).apply(fieldName)

        result.value.value mustBe "街道"
        result.errors.head.message mustEqual postalCodeMaxLengthKey
      }

      s"must not include unicode character" in {
        (161 to 255).map(_.toChar).foreach(o => {
          val result = form.bind(Map(fieldName -> s"N${o}5 6JK", countryCode -> "GB")).apply(fieldName)

          result.errors.head.key mustEqual fieldName
          result.errors.head.message mustEqual postalCodeMaxLengthKey
        })
      }

    }

    "when is optional (countryCode is not GB or empty)" - {

      val postalCodMaxLength = 10

      val optionalTable = Table(
        ("description", "countryValue"),
        ("not GB", "IT"),
        ("not present", "")
      )

      forAll(optionalTable) {
        (
          description: String,
          countryCode: String
        ) =>
          s"must bind valid data when countryCode is $description" in {
            val result = form.bind(Map(fieldName -> "NE 123", countryCode -> countryCode)).apply(fieldName)

            result.value.value mustBe "NE 123"
            result.errors mustBe empty
          }

          s"must bind empty data when countryCode is $description" in {
            val result = form.bind(Map(fieldName ->"", countryCode -> countryCode)).apply(fieldName)

            result.value.value mustBe ""
            result.errors mustBe empty
          }

          s"must return empty when countryCode is $description" in {
            val result = form.bind(Map(countryCode -> countryCode)).apply(fieldName)

            result.value mustBe None
            result.errors mustBe empty
          }

          s"must have maximum length of 10 when countryCode is $description" in {
            val result = form.bind(Map(fieldName -> List.fill(postalCodMaxLength + 1)("b").mkString, countryCode -> countryCode))
              .apply(fieldName)

            result.errors.head.key mustEqual fieldName
            result.errors.head.message mustEqual nonUkPostalCodeMaxLengthKey
          }
      }
    }
  }

  ".countryCode" - {
    val fieldName = "countryCode"

    "must bind valid data" in {
      val result = form.bind(Map(fieldName -> "IT")).apply(fieldName)

      result.value.value mustBe "IT"
      result.errors mustBe empty
    }

    "must error when empty" in {
      val result = form.bind(emptyForm).apply(fieldName)

      result.errors.head.key mustEqual fieldName
      result.errors.head.message mustEqual countryCodeRequiredKey
    }

    "must error when binding blank values" in {
      val result = form.bind(Map(countryCode -> "")).apply(fieldName)

      result.errors.head.key mustEqual fieldName
      result.errors.head.message mustEqual countryCodeRequiredKey
    }
  }
}
