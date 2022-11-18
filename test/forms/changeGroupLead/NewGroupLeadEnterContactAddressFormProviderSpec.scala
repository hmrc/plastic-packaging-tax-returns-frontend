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
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider.{addressLine4RequiredKey, addressLineRequiredKey, countryCode, countryCodeInvalidCharKey, countryCodeLengthKey, countryCodeRequiredKey, postalCode, postalCodeMaxLengthKey, postalCodeRequiredKey}
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
    ("addressLine1 field", "addressLine1", maxLength, addressLineRequiredKey, invalidKey(1), maxLengthKey(1), "mandatory"),
    ("addressLine2 field", "addressLine2", maxLength, addressLineRequiredKey, invalidKey(2), maxLengthKey(2), "mandatory"),
    ("addressLine3 field", "addressLine3", maxLength, addressLineRequiredKey, invalidKey(3), maxLengthKey(3), "optional"),
    ("addressLine4 field", "addressLine4", maxLength, addressLine4RequiredKey, invalidKey(4), maxLengthKey(4), "mandatory"),
    ("countryCode field", "countryCode", maxLength, countryCodeRequiredKey, countryCodeInvalidCharKey, countryCodeLengthKey, "mandatory")
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
            result.value mustBe Some("")
          }
        }

        s"$description must have maximum length of ${maxStringLength}" in {
          val result = form.bind(Map(fieldName -> List.fill(maxStringLength + 1)("b").mkString)).apply(fieldName)

          result.errors.head.key mustEqual fieldName
          result.errors.head.message mustEqual maxLengthKey
        }

        s"$description can include space" in {
          val result = form.bind(Map(fieldName -> "ne5 6th")).apply(fieldName)

          result.value.value mustBe "ne5 6th"
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
    val minLength = 5
    val maxLength = 8
    val fieldName = "postalCode"

    "when is mandatory (countryCode is GB)" - {
      "must bind valid data" in {
        val result = form.bind(Map(fieldName -> "NE4 7FG")).apply(fieldName)

        result.value.value mustBe "NE4 7FG"
        result.errors mustBe empty
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
        ("must not include lower character", "Ne5 7TH"),
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

      val postalCodMaxLength = 35

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

          s"must have maximum length of 35 when countryCode is $description" in {
            val result = form.bind(Map(fieldName -> List.fill(postalCodMaxLength + 1)("b").mkString, countryCode -> countryCode))
              .apply(fieldName)

            result.errors.head.key mustEqual fieldName
            result.errors.head.message mustEqual postalCodeMaxLengthKey
          }
      }
    }
  }
}
