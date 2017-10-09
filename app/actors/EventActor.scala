package actors

import java.net.URI

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.daos.{EventDAO, ParticipantDAO}
import models.gift.Event
import models.user.User
import play.api.Logger
import play.api.libs.json.Json
import redis.RedisClient

import scala.concurrent.duration._

object EventActor {

  /**
   * Definition for the controller to create the websocket
   */
  def props(event: Event, user: User, out: ActorRef, eventDAO: EventDAO) =
    Props(new EventActor(event, user, out, eventDAO))
}

class EventActor(
  event: Event,
  user: User,
  out: ActorRef,
  eventDAO: EventDAO
) extends Actor with ActorLogging {

  val channelPrefix = event.id.get.toString
  val commentsChannel = s"$channelPrefix.comments"
  val giftsChannel = s"$channelPrefix.gifts"
  val redisUri = new URI(context.system.settings.config.getString("redis.uri"))
  val redisHost = redisUri.getHost
  val redisPort = redisUri.getPort
  val scheduler = context.system.scheduler

  val maybePassword = redisUri.getUserInfo match {
    case null => None
    case str => Some(str.split(":")(1))
  }
  val pubSub = context.watch(context.actorOf(PubSubActor.props(channelPrefix, user, redisHost, redisPort, maybePassword)))
  val redis = RedisClient(redisHost, redisPort, maybePassword)(context.system)

  def receive = {
    case _: String =>
      // ignore messages received for now
      ()

    case m: PubSubActor.PubMessage =>
      val json = Json.parse(m.data.utf8String)
      m.channel match {
        case `commentsChannel` =>
          out ! Json.obj("comment" -> json).toString

        case `giftsChannel` =>
          out ! Json.obj("gift" -> json).toString
      }
  }
}
