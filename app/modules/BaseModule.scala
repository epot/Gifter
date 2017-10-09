package modules

import com.google.inject.AbstractModule
import models.daos._
import models.services.{AuthTokenService, AuthTokenServiceImpl, EventNotificationService, EventNotificationServiceImpl}
import net.codingwell.scalaguice.ScalaModule

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[EventDAO].to[EventDAOImpl]
    bind[GiftDAO].to[GiftDAOImpl]
    bind[HistoryDAO].to[HistoryDAOImpl]
    bind[ParticipantDAO].to[ParticipantDAOImpl]
    bind[CommentDAO].to[CommentDAOImpl]
    bind[NotificationDAO].to[NotificationDAOImpl]
    bind[EventNotificationService].to[EventNotificationServiceImpl]
  }
}
