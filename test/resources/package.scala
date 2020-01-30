/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import models._
import models.domain._
import models.messages.{Message, MessageSearchResults}
import models.registration.{AdminOrganisationAccountDetails, IndividualUserAccountDetails}
import models.searchApi.{AgentAuthClient, AgentAuthorisation, OwnerAuthAgent, OwnerAuthorisation}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, _}

package object resources {

  implicit def getArbitrary[T](t: Gen[T]): T = t.sample.get

  implicit val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary(
    Gen.choose(0L, 375584 /* approx 1/1/2999 */ ).map(LocalDate.ofEpochDay(_)))

  def dateInPast: Gen[LocalDate] =
    Gen
      .choose(0L, Instant.now().toEpochMilli)
      .map(l => Instant.ofEpochMilli(l).atZone(ZoneId.systemDefault).toLocalDate)

  def shortString = Gen.listOfN(20, Gen.alphaChar).map(_.mkString)

  private def postcode = Gen.listOfN(8, Gen.alphaChar).map(_.mkString.toUpperCase)

  def randomEmail = {
    val mailbox: String = shortString
    val domain: String = shortString
    val tld: String = shortString
    s"$mailbox@$domain.$tld"
  }

  def positiveLong = Gen.choose(0L, Long.MaxValue)

  def positiveInt = Gen.choose(0, Int.MaxValue)

  def dateAfterApril2017: Gen[LocalDate] =
    for {
      year  <- Gen.choose(2017, 2100)
      month <- Gen.choose(1, 12)
      day   <- Gen.choose(1, 28)
    } yield LocalDate.of(year, month, day)

  val propertyAddressGen: Gen[PropertyAddress] = for {
    lines    <- Gen.nonEmptyListOf(shortString)
    postcode <- shortString
  } yield PropertyAddress(lines, postcode)

  implicit val arbitraryPropertyAddress = Arbitrary(for { p <- propertyAddressGen } yield p)

  val propertyGen: Gen[Property] = for {
    uarn                      <- positiveLong
    billingAuthorityReference <- shortString
    address                   <- propertyAddressGen
    specialCategoryCode       <- shortString
    description               <- shortString
    bulkClassIndicator        <- arbitrary[Char].map(_.toString)
  } yield Property(uarn, billingAuthorityReference, address.toString, specialCategoryCode)
  implicit val arbitraryProperty = Arbitrary(for { p <- propertyGen } yield p)

  val capacityTypeGen: Gen[CapacityType] = Gen.oneOf(Owner, OwnerOccupier, Occupier)
  implicit val arbitratyCapacityType = Arbitrary(capacityTypeGen)

  val capacityDeclarationGen: Gen[CapacityDeclaration] = for {
    capacity             <- arbitrary[CapacityType]
    interestedBefore2017 <- arbitrary[Boolean]
    fromDate             <- dateAfterApril2017
    stillInterested      <- arbitrary[Boolean]
    days                 <- Gen.choose(1, 5000)
    toDate = fromDate.plusDays(days)
  } yield
    CapacityDeclaration(
      capacity,
      interestedBefore2017,
      if (interestedBefore2017) None else Some(fromDate),
      stillInterested,
      if (stillInterested) None else Some(toDate))
  implicit val arbitraryCapacityDeclaration = Arbitrary(capacityDeclarationGen)

  val addressGen: Gen[Address] = for {
    id       <- arbitrary[Int]
    line1    <- shortString
    line2    <- shortString
    line3    <- shortString
    line4    <- shortString
    postcode <- postcode
  } yield Address(Some(id), line1, line2, line3, line4, postcode)

  implicit val arbitraryAddress = Arbitrary(addressGen)
  val detailedAddressGen: Gen[DetailedAddress] = for {
    addressUnitId             <- arbitrary[Int]
    nonAbpAddressId           <- arbitrary[Int]
    organisationName          <- shortString
    departmentName            <- shortString
    subBuildingName           <- shortString
    buildingName              <- shortString
    buildingNumber            <- shortString
    dependentThoroughfareName <- shortString
    thoroughfareName          <- shortString
    doubleDependentLocality   <- shortString
    dependentLocality         <- shortString
    postTown                  <- shortString
    postcode                  <- postcode
  } yield
    DetailedAddress(
      Some(addressUnitId),
      Some(nonAbpAddressId),
      Some(organisationName),
      Some(departmentName),
      Some(subBuildingName),
      Some(buildingName),
      Some(buildingNumber),
      Some(dependentThoroughfareName),
      Some(thoroughfareName),
      Some(doubleDependentLocality),
      Some(dependentLocality),
      postTown,
      postcode
    )
  implicit val arbitraryDetailedAddress = Arbitrary(detailedAddressGen)

  val individualDetailsGen: Gen[IndividualDetails] = for {
    fistName  <- shortString
    lastName  <- shortString
    phone1    <- Gen.listOfN(8, Gen.numChar)
    phone2    <- Gen.option(Gen.listOfN(8, Gen.numChar).map(_.mkString))
    addressId <- arbitrary[Int]
  } yield IndividualDetails(fistName, lastName, randomEmail, phone1.mkString, phone2, addressId)
  implicit val arbitraryIndividualDetails = Arbitrary(individualDetailsGen)

  val individualGen: Gen[DetailedIndividualAccount] = for {
    externalId        <- shortString
    trustId           <- shortString
    organisationId    <- arbitrary[Int]
    individualId      <- arbitrary[Int]
    individualDetails <- arbitrary[IndividualDetails]
  } yield DetailedIndividualAccount(externalId, trustId, organisationId, individualId, individualDetails)
  implicit val arbitraryIndividual = Arbitrary(individualGen)

  val groupAccountGen: Gen[GroupAccount] = for {
    id          <- positiveInt
    groupId     <- shortString
    companyName <- arbitrary[String]
    addressId   <- arbitrary[Int]
    phone       <- Gen.listOfN(8, Gen.numChar)
    isAgent     <- arbitrary[Boolean]
    agentCode   <- positiveLong
  } yield
    GroupAccount(
      id,
      groupId,
      companyName,
      addressId,
      randomEmail,
      phone.mkString,
      isAgent,
      Some(agentCode).filter(_ => isAgent))
  implicit val arbitraryGroupAccount = Arbitrary(groupAccountGen)

  val capacityGen: Gen[Capacity] = for {
    capacity <- arbitrary[CapacityType]
    fromDate <- arbitrary[LocalDate]
    toDate   <- arbitrary[Option[LocalDate]]
  } yield Capacity(capacity, fromDate, toDate)
  implicit val arbitraryCapacity = Arbitrary(capacityGen)

  val assessmentGen: Gen[Assessment] = for {
    linkId                    <- arbitrary[Int]
    asstRef                   <- positiveLong
    listYear                  <- shortString
    uarn                      <- positiveLong
    effectiveDate             <- arbitrary[LocalDate]
    rateableValue             <- positiveLong
    address                   <- arbitrary[PropertyAddress]
    billingAuthorityReference <- shortString
    capacity                  <- arbitrary[Capacity]
    description               <- shortString
  } yield
    models.Assessment(
      linkId,
      asstRef,
      listYear,
      uarn,
      effectiveDate,
      Some(rateableValue),
      address,
      billingAuthorityReference,
      None,
      None,
      capacity)
  implicit val arbitraryAssessment = Arbitrary(assessmentGen)

  val agentPermissionGen: Gen[AgentPermission] = Gen.oneOf(StartAndContinue, NotPermitted)
  implicit val agentPermissionType = Arbitrary(agentPermissionGen)

  val representationStatusGen: Gen[RepresentationStatus] = Gen.oneOf(RepresentationApproved, RepresentationPending)
  implicit val representationStatusType = Arbitrary(representationStatusGen)

  val party: Gen[Party] = for {
    authorisedPartyId   <- arbitrary[Long]
    agentCode           <- arbitrary[Long]
    organisationName    <- shortString
    organisationId      <- arbitrary[Long]
    checkPermission     <- arbitrary[AgentPermission]
    challengePermission <- arbitrary[AgentPermission]
  } yield
    models.Party(authorisedPartyId, agentCode, organisationName, organisationId, checkPermission, challengePermission)
  implicit val arbitraryParty = Arbitrary(party)

  val clientPropertyGen: Gen[ClientProperty] = for {
    ownerOrganisationId       <- arbitrary[Long]
    ownerOrganisationName     <- shortString
    billingAuthorityReference <- shortString
    authorisedPartyId         <- arbitrary[Long]
    authorisationId           <- arbitrary[Long]
    authorisationStatus       <- arbitrary[Boolean]
    authorisedPartyStatus     <- arbitrary[RepresentationStatus]
    checkPermission           <- shortString
    challengePermission       <- shortString
    address                   <- shortString
  } yield
    models.ClientProperty(
      ownerOrganisationId,
      ownerOrganisationName,
      billingAuthorityReference,
      authorisedPartyId,
      authorisationId,
      authorisationStatus,
      authorisedPartyStatus,
      checkPermission,
      challengePermission,
      address
    )
  implicit val arbitraryClientProperty = Arbitrary(clientPropertyGen)

  val propertyLinkGen: Gen[PropertyLink] = for {
    linkId            <- arbitrary[Int]
    submissionId      <- shortString
    uarn              <- positiveLong
    organisationId    <- arbitrary[Int]
    address           <- arbitrary[PropertyAddress]
    capacity          <- arbitrary[Capacity]
    linkedDate        <- arbitrary[LocalDate]
    pending           <- arbitrary[Boolean]
    assessment        <- Gen.nonEmptyListOf(arbitrary[Assessment])
    userActingAsAgent <- arbitrary[Boolean]
    agents            <- Gen.listOf(arbitrary[Party])
  } yield {
    PropertyLink(
      authorisationId = linkId,
      submissionId = submissionId,
      uarn = uarn,
      address = address.toString,
      agents = agents)
  }
  implicit val arbitraryPropertyLink = Arbitrary(propertyLinkGen)

  val propertyRepresentationGen: Gen[PropertyRepresentation] = for {
    authorisationId           <- arbitrary[Long]
    billingAuthorityReference <- shortString
    submissionId              <- shortString
    organisationId            <- arbitrary[Long]
    organisationName          <- shortString
    address                   <- arbitrary[PropertyAddress]
    checkPermission           <- arbitrary[AgentPermission]
    challengePermission       <- arbitrary[AgentPermission]
    status                    <- arbitrary[RepresentationStatus]
  } yield {
    PropertyRepresentation(
      authorisationId = authorisationId,
      billingAuthorityReference = billingAuthorityReference,
      submissionId = submissionId,
      organisationId = organisationId,
      organisationName = organisationName,
      address = address.toString,
      checkPermission = checkPermission,
      challengePermission = challengePermission,
      status = status
    )
  }
  implicit val arbitraryPropertyRepresentation = Arbitrary(propertyRepresentationGen)

  private val ninoGen: Gen[Nino] = for {
    prefix <- (for {
               prefix1 <- Gen.oneOf(('A' to 'Z').filterNot(List('D', 'F', 'I', 'Q', 'U', 'V').contains))
               prefix2 <- Gen.oneOf(('A' to 'Z').filterNot(List('D', 'F', 'I', 'O', 'Q', 'U', 'V').contains))
             } yield (s"$prefix1$prefix2")) retryUntil (!List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ").contains(_))
    number <- Gen.listOfN(6, Gen.numChar)
    suffix <- Gen.oneOf('A' to 'D')
  } yield Nino(s"$prefix${number.mkString}$suffix".grouped(2).mkString(" "))

  implicit val arbitraryNino = Arbitrary(ninoGen)

  private val ivDetailsGen: Gen[IVDetails] = for {
    firstName <- shortString
    lastName  <- shortString
    dob       <- arbitrary[LocalDate]
    nino      <- arbitrary[Nino]
  } yield IVDetails(firstName, lastName, Some(dob), Some(nino))

  implicit val arbitraryIVDetails = Arbitrary(ivDetailsGen)

  private val accountsGenerator = for {
    group      <- arbitrary[GroupAccount]
    individual <- arbitrary[DetailedIndividualAccount]
  } yield Accounts(group, individual)

  implicit val arbitraryAccounts: Arbitrary[Accounts] = Arbitrary(accountsGenerator)

  private val personalDetailsGen: Gen[PersonalDetails] = for {
    firstName <- shortString
    lastName  <- shortString
    dob       <- dateInPast
    nino      <- arbitrary[Nino]
    email = randomEmail
    phone   <- Gen.listOfN(8, Gen.numChar)
    address <- arbitrary[Address]
  } yield PersonalDetails(firstName, lastName, dob, nino, email, email, phone.mkString, None, address)

  implicit val arbitraryPersonalDetails: Arbitrary[PersonalDetails] = Arbitrary(personalDetailsGen)

  private val enrolmentOrgAccountDetailsGen: Gen[AdminOrganisationAccountDetails] = for {
    firstName   <- shortString
    lastName    <- shortString
    dob         <- dateInPast
    companyName <- shortString
    nino        <- arbitrary[Nino]
    email = randomEmail
    phone   <- Gen.listOfN(8, Gen.numChar)
    address <- arbitrary[Address]
    isAgent <- arbitrary[Boolean]
  } yield
    AdminOrganisationAccountDetails(
      firstName,
      lastName,
      companyName,
      address,
      dob,
      nino,
      phone.mkString,
      email,
      email,
      Some(isAgent))

  implicit val arbitraryEnrolmentOrganisationAccountDetails: Arbitrary[AdminOrganisationAccountDetails] = Arbitrary(
    enrolmentOrgAccountDetailsGen)

  private val enrolmentIndAccountDetailsGen: Gen[IndividualUserAccountDetails] = for {
    firstName   <- shortString
    lastName    <- shortString
    dob         <- dateInPast
    companyName <- shortString
    nino        <- arbitrary[Nino]
    email = randomEmail
    phone   <- Gen.listOfN(8, Gen.numChar)
    address <- arbitrary[Address]
    isAgent <- arbitrary[Boolean]
  } yield
    IndividualUserAccountDetails(
      firstName,
      lastName,
      address,
      dob,
      nino,
      phone.mkString,
      phone.mkString,
      email,
      email,
      None)

  implicit val arbitraryEnrolmentIndividualAccountDetails: Arbitrary[IndividualUserAccountDetails] = Arbitrary(
    enrolmentIndAccountDetailsGen)

  private val linkingSessionGen: Gen[LinkingSession] = for {
    address            <- shortString
    uarn               <- positiveLong
    envelopeId         <- shortString
    submissionId       <- shortString
    personId           <- positiveLong
    declaration        <- capacityDeclarationGen
    uploadEvidenceData <- Gen.const(UploadEvidenceData.empty)
  } yield LinkingSession(address, uarn, submissionId, personId, declaration, uploadEvidenceData)

  implicit val arbitraryLinkinSession: Arbitrary[LinkingSession] = Arbitrary(linkingSessionGen)

  val draftCaseGenerator: Gen[DraftCase] = for {
    id <- shortString
    url <- shortString.map { s =>
            s"/business-rates-check/build/$s"
          }
    address        <- arbitrary[PropertyAddress]
    effectiveDate  <- arbitrary[LocalDate]
    checkType      <- shortString
    expirationDate <- arbitrary[LocalDate]
    propertyLinkId <- positiveLong
    assessmentRef  <- positiveLong
    baRef          <- shortString
  } yield DraftCase(id, url, address, effectiveDate, checkType, expirationDate, propertyLinkId, assessmentRef, baRef)

  def randomDraftCase: DraftCase = draftCaseGenerator.sample.get

  val agentAuthClientGen: Gen[AgentAuthClient] = for {
    organisationId   <- arbitrary[Long]
    organisationName <- shortString
  } yield {
    AgentAuthClient(organisationId, organisationName)
  }
  implicit val agentAuthClient = Arbitrary(agentAuthClientGen)

  val ownerAuthAgentGen: Gen[OwnerAuthAgent] = for {
    authorisedPartyId <- arbitrary[Long]
    organisationId    <- arbitrary[Long]
    organisationName  <- shortString
    status <- Gen.oneOf(
               RepresentationApproved.name,
               RepresentationDeclined.name,
               RepresentationPending.name,
               RepresentationRevoked.name)
    agentCode <- arbitrary[Long]
  } yield {
    OwnerAuthAgent(
      authorisedPartyId,
      organisationId,
      organisationName,
      status,
      StartAndContinue,
      StartAndContinue,
      agentCode)
  }
  implicit val ownerAuthAgent = Arbitrary(ownerAuthAgentGen)

  val agentAuthorisationGen: Gen[AgentAuthorisation] = for {
    authorisationId   <- arbitrary[Long]
    authorisedPartyId <- arbitrary[Long]
    uarn              <- arbitrary[Long]
    status <- Gen.oneOf(
               RepresentationApproved.name,
               RepresentationDeclined.name,
               RepresentationPending.name,
               RepresentationRevoked.name)
    submissionId         <- shortString
    address              <- arbitrary[PropertyAddress]
    localAuthorityRef    <- shortString
    client               <- arbitrary[AgentAuthClient]
    representationStatus <- arbitrary[String]
  } yield {
    AgentAuthorisation(
      authorisationId = authorisationId,
      authorisedPartyId = authorisedPartyId,
      status = status,
      uarn = uarn,
      submissionId = submissionId,
      address = address.toString,
      localAuthorityRef = localAuthorityRef,
      client = client,
      representationStatus = representationStatus
    )
  }
  implicit val arbitraryAgentAuthorisationGen = Arbitrary(agentAuthorisationGen)

  val ownerAuthorisationGen: Gen[OwnerAuthorisation] = for {
    id   <- arbitrary[Long]
    uarn <- arbitrary[Long]
    status <- Gen.oneOf(
               RepresentationApproved.name,
               RepresentationDeclined.name,
               RepresentationPending.name,
               RepresentationRevoked.name)
    submissionId      <- shortString
    address           <- shortString
    localAuthorityRef <- shortString
    agents            <- Gen.listOfN(1, arbitrary[OwnerAuthAgent])
  } yield {
    OwnerAuthorisation(
      authorisationId = id,
      status = status,
      uarn = uarn,
      submissionId = submissionId,
      address = address.toString,
      localAuthorityRef = localAuthorityRef,
      agents = agents)
  }
  implicit val arbitraryOwnerAuthorisationGen = Arbitrary(ownerAuthorisationGen)

  val messageGen: Gen[Message] = for {
    id             <- shortString
    recipientOrgId <- arbitrary[Long]
    templateName   <- shortString
    clientOrgId    <- arbitrary[Long]
    clientName     <- shortString
    agentOrgId     <- arbitrary[Long]
    agentOrgName   <- arbitrary[String]
    caseReference  <- shortString
    submissionId   <- shortString
    address        <- shortString
    subject        <- shortString
    messageType    <- shortString
    address        <- shortString
  } yield {
    Message(
      id = id,
      recipientOrgId = recipientOrgId,
      templateName = templateName,
      clientOrgId = Some(clientOrgId),
      clientName = Some(clientName),
      agentOrgId = Some(agentOrgId),
      agentName = Some(agentOrgName),
      caseReference = caseReference,
      submissionId = submissionId,
      timestamp = LocalDateTime.now.minusDays(1),
      address = address,
      effectiveDate = LocalDateTime.now.minusYears(1),
      subject = subject,
      lastRead = Some(LocalDateTime.now),
      messageType = messageType
    )
  }

  implicit val arbitraryMessageGen = Arbitrary(messageGen)

  val messageSearchResultsGen: Gen[MessageSearchResults] = for {
    start    <- Gen.choose[Int](min = 1, max = 30)
    size     <- Gen.choose[Int](min = start, max = 30)
    messages <- Gen.listOfN(size, arbitrary[Message])
  } yield {
    MessageSearchResults(start = start, size = size, messages = messages)
  }

  implicit val arbitraryMessageSearchResultsGen = Arbitrary(messageSearchResultsGen)

  val updatedOrganisationAccountGen: Gen[UpdatedOrganisationAccount] = for {
    governmentGatewayGroupId    <- shortString
    addressUnitId               <- arbitrary[Long]
    representativeFlag          <- arbitrary[Boolean]
    organisationName            <- shortString
    organisationEmailAddress    <- shortString
    organisationTelephoneNumber <- shortString
    changedByGGExternalId       <- shortString
  } yield {
    UpdatedOrganisationAccount(
      governmentGatewayGroupId = governmentGatewayGroupId,
      addressUnitId = addressUnitId,
      representativeFlag = representativeFlag,
      organisationName = organisationName,
      organisationEmailAddress = organisationEmailAddress,
      organisationTelephoneNumber = organisationTelephoneNumber,
      effectiveFrom = Instant.now(),
      changedByGGExternalId = changedByGGExternalId
    )
  }

  implicit val arbitraryUpdatedOrganisationAccountGen = Arbitrary(updatedOrganisationAccountGen)

}
