package models.services

import javax.inject.Inject
import akka.actor.ActorSystem
import models.gift.{Comment, Gift}
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class EventNotificationServiceImpl @Inject()(configuration: Configuration, _system: ActorSystem,
                                             implicit val ex: ExecutionContext) extends EventNotificationService {


  // not doing anything for now, redis is out of the picture for now
  def publishComment(eventId: Long, comment: Comment) = {
    Future.successful(())
  }
  def publishGift(gift: Gift) = {
    Future.successful(())
  }
}
