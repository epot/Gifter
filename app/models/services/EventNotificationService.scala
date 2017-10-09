package models.services

import models.gift.{Comment, Gift}

import scala.concurrent.Future
import scala.language.postfixOps

trait EventNotificationService {

  def publishComment(eventId: Long, comment: Comment): Future[Unit]
  def publishGift(gift: Gift): Future[Unit]
}
