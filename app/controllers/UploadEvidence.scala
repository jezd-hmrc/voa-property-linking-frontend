/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers
import javax.inject.Inject

import config.{Environment, Wiring}
import connectors.{FileInfo, LinkBasis, NoEvidenceFlag, OtherEvidenceFlag}
import connectors.fileUpload.{FileUpload, FileUploadConnector}
import form.EnumMapping
import models.{DoesHaveEvidence, DoesNotHaveEvidence, EvidenceType, HasEvidence}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.Files.TemporaryFile
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContent
import play.api.mvc.MultipartFormData.FilePart
import session.{LinkingSession, LinkingSessionRequest, WithLinkingSession}
import views.helpers.Errors

import scala.concurrent.Future

class UploadEvidence @Inject() (val fileUploadConnector: FileUpload) extends PropertyLinkingController {
  lazy val propertyLinkConnector = Wiring().propertyLinkConnector
  lazy val withLinkingSession = Wiring().withLinkingSession
  lazy val fileSystemConnector = Wiring().fileSystemConnector

  def show() = withLinkingSession { implicit request =>
    Ok(views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form)))
  }

  def submit() = withLinkingSession { implicit request =>
    UploadEvidence.form.bindFromRequest().fold(
      error => BadRequest(views.html.uploadEvidence.show(UploadEvidenceVM(error))),
      uploaded => uploaded.hasEvidence match {
        case DoesHaveEvidence => {
          val filePart = request.request.body.asMultipartFormData.get.file("evidence[]")
          uploadIfNeeded(filePart) flatMap { x =>
            x match {
              case FilesAccepted =>
                val fileInfo = FileInfo(filePart.map(_.filename).getOrElse("FilenameNotFound"), uploaded.evidenceType.name)
                val refId = java.util.UUID.randomUUID.toString
                requestLink(refId, OtherEvidenceFlag, Some(fileInfo))
                  .map(_ => Redirect(routes.UploadEvidence.evidenceUploaded()))
              case FilesUploadFailed => BadRequest(
                views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", Errors.uploadedFiles))))
              case FilesMissing => BadRequest(views.html.uploadEvidence.show(UploadEvidenceVM(UploadEvidence.form.withError("evidence[]", Errors.missingFiles))))
            }
          }
        }
        case DoesNotHaveEvidence => {
          val refId = java.util.UUID.randomUUID.toString
          requestLink(refId, NoEvidenceFlag, None)
            .map( _ => Redirect(routes.UploadEvidence.noEvidenceUploaded(refId))
            )
        }
      }
    )
  }

  private def requestLink(refId: String, linkBasis: LinkBasis, fileInfo: Option[FileInfo])(implicit r: LinkingSessionRequest[AnyContent]) =
    propertyLinkConnector.linkToProperty(r.ses.claimedProperty,
      r.groupId, r.ses.declaration.getOrElse(throw new Exception("No declaration")),
      refId, linkBasis, fileInfo
    )

  private def uploadIfNeeded(filePart: Option[FilePart[TemporaryFile]])
                            (implicit request: LinkingSessionRequest[AnyContent]): Future[EvidenceUploadResult] = {
    filePart.map(part => {
      val envId = request.ses.envelopeId
      val contentType = part.contentType.getOrElse("application/octet-stream")
      fileUploadConnector.uploadFile(envId, part.filename, contentType,part.ref.file )
        .map (_ => FilesAccepted )
        .recover{ case _ => FilesUploadFailed}
    }).getOrElse(Future.successful(FilesMissing))

    val files: Seq[FilePart[TemporaryFile]] = request.request.body.asMultipartFormData.get.files.filter(_.key.startsWith("evidence[]"))
    if (files.isEmpty) FilesMissing
    else {
      val res = Future.sequence(files.map( filepart => {
        val envId = request.ses.envelopeId
        val contentType = filepart.contentType.getOrElse("application/octet-stream")
        fileUploadConnector.uploadFile(envId, filepart.filename, contentType,filepart.ref.file)
          .map(_ =>FilesAccepted)
          .recover{case _ => FilesUploadFailed}
      }))
      res.map(results => if (results.contains(FilesUploadFailed)) FilesUploadFailed else FilesAccepted)
    }
  }

  def evidenceUploaded() = withLinkingSession { implicit request =>
    fileUploadConnector.closeEnvelope(request.ses.envelopeId).flatMap( _=>
      Wiring().sessionRepository.remove().map( _ =>
        Ok(views.html.linkingRequestSubmitted())
      )
    )
  }

  def noEvidenceUploaded(refId: String) = withLinkingSession { implicit request =>
    fileUploadConnector.closeEnvelope(request.ses.envelopeId).flatMap( _=>
      Wiring().sessionRepository.remove().map( _ =>
        Ok(views.html.uploadEvidence.noEvidenceUploaded(refId))
      )
    )
  }

}
object UploadEvidence {
  lazy val form = Form(mapping(
    "hasEvidence" -> EnumMapping(HasEvidence),
    "evidenceType" -> EnumMapping(EvidenceType)
  )(UploadedEvidence.apply)(UploadedEvidence.unapply))

}


case class UploadedEvidence(hasEvidence: HasEvidence, evidenceType: EvidenceType)

case class UploadEvidenceVM(form: Form[_])

sealed trait EvidenceUploadResult
case object FilesAccepted     extends EvidenceUploadResult
case object FilesMissing      extends EvidenceUploadResult
case object FilesUploadFailed extends EvidenceUploadResult
