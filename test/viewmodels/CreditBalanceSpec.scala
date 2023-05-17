package viewmodels

import models.{CreditBalance, TaxablePlastic}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

class CreditBalanceSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  private val creditBalance = CreditBalance(
    availableCreditInPounds = 1, 
    totalRequestedCreditInPounds = 2, 
    totalRequestedCreditInKilograms = 3, 
    canBeClaimed = true, 
    credit = Map(
      "a-key" -> TaxablePlastic(weight = 11, moneyInPounds = 22, taxRate = 33),
      "b-key" -> TaxablePlastic(weight = 44, moneyInPounds = 55, taxRate = 66)
    )
  )

  "it" should {
    "look up the correct year" in {
      creditBalance.creditForYear("b-key") mustBe TaxablePlastic(weight = 44, moneyInPounds = 55, taxRate = 66)
    }
    "return zeros for a year that isn't there" in {
      creditBalance.creditForYear("z-key") mustBe TaxablePlastic.zero
    }
  }
  
}
