package useCaseSpecs

import useCaseSpecs.utils._

class EvidenceUploaded extends FrontendTest {

  import TestData._

  "Given an interested person has uploaded some evidence for a property" - {
    implicit val sid: SessionId = java.util.UUID.randomUUID.toString
    implicit val session = GGSession(userId, token)
    HTTP.stubKeystoreSession(SessionDocument(nonSelfCertProperty, Some(declaration), selfCertifyComplete = Some(false)), Seq(Account(userId, false)))
    HTTP.stubAuthentication(session)

    "When they arrive at the evidence submitted page" - {
      val page = Page.get("/property-linking/evidence-uploaded")

      "They see confirmation that their property linking request has been submitted" in {
        page.mustContainSuccessSummary(s"Thank you for your request which has been submitted to the Valuation Office Agency.")
      }

      "And the page contains a link to the dashboard" in {
        page.mustContainLink("#backToDashBoard", "/property-linking/manage-properties")
      }
    }
  }

  object TestData {
    lazy val baRef = "sfku03802342"
    lazy val uarn = "uarn03802342"
    lazy val address = Address(Seq("leen1", "leen2", "leen3"), "AA11 1AA")
    lazy val userId = "389u4asldkjfasljdf"
    lazy val token = "tasfnmsfk"
    lazy val formattedAddress = "leen1, leen2, leen3, AA11 1AA"
    lazy val nonSelfCertProperty = Property(uarn, baRef, address, false, true)
    lazy val declaration = CapacityDeclaration("occupier", "2001-01-01", None)
  }

}
