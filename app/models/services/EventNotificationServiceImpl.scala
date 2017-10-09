package models.services

import java.net.URI
import javax.inject.Inject

import akka.actor.ActorSystem
import models.gift.{Comment, Gift}
import models.JsonFormat._
import play.api.Configuration
import play.api.libs.json.Json
import redis.RedisClient

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class EventNotificationServiceImpl @Inject()(configuration: Configuration, _system: ActorSystem,
                                             implicit val ex: ExecutionContext) extends EventNotificationService {

  val redisUri = new URI(configuration.get[String]("redis.uri"))
  val redisHost = redisUri.getHost
  val redisPort = redisUri.getPort

  val maybePassword = redisUri.getUserInfo match {
    case null => None
    case str => Some(str.split(":")(1))
  }
  val redis = RedisClient(redisHost, redisPort, maybePassword)(_system)

  def getCommentsChannel(eventId: Long) = s"$eventId.comments"
  def getGiftsChannel(eventId: Long) = s"$eventId.gifts"

  def publishComment(eventId: Long, comment: Comment) = {
    val caca = getCommentsChannel(eventId)
    redis.publish(getCommentsChannel(eventId), Json.toJson(comment).toString).map(_ => ())
  }
  def publishGift(gift: Gift) = {
    redis.publish(getGiftsChannel(gift.eventid), Json.toJson(gift).toString).map(_ => ())
  }
}
