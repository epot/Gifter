package actors

import java.net.{ InetSocketAddress, URI, URL }

import actors.PubSubActor.PubMessage
import akka.actor.{ ActorLogging, Props }
import akka.util.ByteString
import models.user.User
import play.api.Logger
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{ Message, PMessage }

object PubSubActor {
  case class PubMessage(channel: String, data: ByteString)

  def props(channelPrefix: String, user: User, redisHost: String, redisPort: Int, maybeRedisPassword: Option[String]) = {

    Props(new PubSubActor(channelPrefix, user, redisHost, redisPort, maybeRedisPassword)).withDispatcher("rediscala.rediscala-client-worker-dispatcher")
  }
}

class PubSubActor(channelPrefix: String, user: User, redisHost: String, redisPort: Int, maybeRedisPassword: Option[String])
  extends RedisSubscriberActor(new InetSocketAddress(redisHost, redisPort), Nil, Seq(s"$channelPrefix.*"), authPassword = maybeRedisPassword, onConnectStatus = _ => { }) with ActorLogging {

  def onMessage(message: Message) {
    log.info(s"pattern message received: ${message.data.utf8String}")
    context.parent ! PubMessage(message.channel, message.data)
  }

  def onPMessage(pmessage: PMessage) {
    log.info(s"pattern message received: ${pmessage.data.utf8String}")
    context.parent ! PubMessage(pmessage.channel, pmessage.data)
  }
}
