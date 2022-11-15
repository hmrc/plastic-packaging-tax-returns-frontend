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
import org.scalacheck.Gen
import play.api.data.FormError


//todo: add test for other fields validation. see SoT
class NewGroupLeadEnterContactAddressFormProviderSpec extends StringFieldBehaviours {


  val allowChars = ('A' to 'Z').toList ++
    ('a' to 'z').toList ++
    ('0' to '9').toList ++
    Seq('&', '-', '`', ''', ',', '.')

  val notAllowedChar = ('!' to '~').mkString.filter(o => !allowChars.contains(o))

  val form = new NewGroupLeadEnterContactAddressFormProvider()()
  private val requiredKey = "newGroupLeadEnterContactAddress.error.addressLine.required"
  private val addressLIneRequiredKey = "newGroupLeadEnterContactAddress.error.addressLine4.required"
  private val maxLengthKey = (n: Int) => s"newGroupLeadEnterContactAddress.error.addressLine${n}.length"
  private val postalCodeMaxLengthKey = "newGroupLeadEnterContactAddress.error.postalCode.inRange"
  private val postalCodeRequiredKey = "newGroupLeadEnterContactAddress.error.postalCode.required"
  private val countryCodeRequiredKey = "newGroupLeadEnterContactAddress.error.countryCode.required"
  private val countryCodeMaxLengthKey = "newGroupLeadEnterContactAddress.error.countryCode.length"
  private val countryCodeInvalidKey = "newGroupLeadEnterContactAddress.error.countryCode.invalid"
  private val invalidKey = (n: Int) => s"newGroupLeadEnterContactAddress.error.addressLine${n}.invalid.line"
  private val maxLength = 35

  case class StrRange(min: Int = 1, max: Int)

  val table = Table(
    ("description", "fieldName", "maxStringLength", "requiresKey", "invalidKey", "maxLengthKey", "optionalOrMandatory"),
    ("addressLine1 field", "addressLine1", StrRange(max = maxLength), requiredKey, invalidKey(1), maxLengthKey(1), "mandatory"),
    ("addressLine2 field", "addressLine2", StrRange(max = maxLength), requiredKey, invalidKey(2), maxLengthKey(2), "mandatory"),
    ("addressLine3 field", "addressLine3", StrRange(max = maxLength), requiredKey, invalidKey(3), maxLengthKey(3), "optional"),
    ("addressLine4 field", "addressLine4", StrRange(max = maxLength), addressLIneRequiredKey,invalidKey(4), maxLengthKey(4), "mandatory"),
    ("postalCode field", "postalCode", StrRange(5, 8), postalCodeRequiredKey, postalCodeMaxLengthKey, postalCodeMaxLengthKey, "optional"),
    ("countryCode field", "countryCode", StrRange(max = maxLength), countryCodeRequiredKey, countryCodeInvalidKey, countryCodeMaxLengthKey, "mandatory")
  )

  forAll(table) {
    (
      description: String,
      fieldName: String,
      maxStringLength: StrRange,
      requiresKey: String,
      invalidKey: String,
      maxLengthKey: String,
      optionalOrMandatory: String
    ) =>

      s".$fieldName len $maxStringLength" - {
        behave like fieldThatBindsValidData(
          form,
          fieldName,
          stringsWithMaxLength(maxStringLength.max, maxStringLength.min, Some(Gen.alphaNumChar))
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
          val result = form.bind(Map(fieldName -> List.fill(maxStringLength.max + 1)("b").mkString)).apply(fieldName)
          result.errors.head.key mustEqual fieldName
          result.errors.head.message mustEqual maxLengthKey
        }

        s"$description can include space" in {
          val newForm = form.bind(Map(fieldName -> "ne5 6th")).apply(fieldName)

          newForm.value.value mustBe "ne5 6th"
          newForm.errors mustBe empty
        }

        notAllowedChar.foreach(o => {

          s"$description must not include '$o' character" in {
            val newForm = form.bind(Map(fieldName -> s"gh${o} Nmh")).apply(fieldName)

            newForm.errors.head.key mustEqual fieldName
            newForm.errors.head.message mustEqual invalidKey
          }
        })

        s"$description must not include foreign character" in {
          forAll(stringsWithMaxLength(maxStringLength.max, maxStringLength.min) -> "validDataItem") {
            dataItem: String =>
              val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
              result.value.value mustBe dataItem
              result.errors.head.message mustEqual invalidKey
          }
        }

        s"$description must not include unicode character" in {
          (161 to 255).map(_.toChar).foreach(o => {
            val newForm = form.bind(Map(fieldName -> s"Ny${o} JKe")).apply(fieldName)

            assertResult(newForm.errors.head.key, s"'${o}' should be an valid character")(fieldName)
            newForm.errors.head.message mustEqual invalidKey
          })
        }
      }
  }


  "postalCode must have minimum length of 5" in {
    val fieldName = "postalCode"
    val result = form.bind(Map(fieldName -> List.fill(4)("b").mkString)).apply(fieldName)

    result.errors.head.key mustEqual fieldName
    result.errors.head.message mustEqual "newGroupLeadEnterContactAddress.error.postalCode.inRange"
  }

  "postal code is mandatory if contryCode is GB" ignore {

    val fieldName = "countryCode"

    val t= Map(
      "addressLine1" -> "test",
      "addressLine2" -> "test1",
      "addressLine3" -> "",
      "addressLine4" -> "test2",
      "postalCode" -> "",
      "countryCode" -> "GB"
    )
    val result = form.bind(t)

    result.errors.head.key mustEqual fieldName
    result.errors.head.message mustEqual postalCodeRequiredKey
  }

  "countryCode should error if only number input" ignore {
    val fieldName = "countryCode"
    val result = form.bind(Map(fieldName -> "12 33")).apply(fieldName)

    result.errors.head.key mustEqual fieldName
    result.errors.head.message mustEqual countryCodeRequiredKey
  }
}
