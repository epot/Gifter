package controllers

import java.net.URL

import javax.inject.Inject
import java.util.UUID

import actors.EventActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.gift._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time.DateTime
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import models.daos._
import models.services.{EventNotificationService, UserService}
import models.user.User
import play.api.libs.json.{JsArray, Json}
import utils.auth._
import models.JsonFormat._
import play.api.Logger
import play.api.libs.streams.ActorFlow
import com.github.nscala_time.time.OrderingImplicits.DateTimeOrdering
import utils.Metrics

import scala.concurrent.{ExecutionContext, Future}

class EventsController @Inject()(components: ControllerComponents,
                                 userService: UserService,
                                 eventDAO: EventDAO,
                                 commentDAO: CommentDAO,
                                 participantDAO: ParticipantDAO,
                                 notificationDAO: NotificationDAO,
                                 giftDAO: GiftDAO,
                                 historyDAO: HistoryDAO,
                                 eventNotificationService: EventNotificationService,
                                 silhouette: Silhouette[DefaultEnv])(
  implicit val system: ActorSystem,
  implicit val mat: Materializer,
  implicit val ex: ExecutionContext
)
  extends AbstractController(components) with I18nSupport {

  val c = Metrics.client

  val eventForm = Form[EventSimple](
    tuple(
      "name" -> nonEmptyText
      ,"dateStr" -> date("dd-MM-yyyy")
      ,"type" -> number(min = Event.Type.min, max = Event.Type.max)
      , "cloneFromEvent" -> optional(longNumber)
      ).transform(
    {/*apply*/
      case (name, dateStr, eventtype, maybeEventId) => {
        EventSimple(name=name, date=new DateTime(dateStr), eventtype=Event.Type.fromId(eventtype), maybeEventId)
      }
    },{ /*unapply*/
      event: EventSimple => (
            event.name,
            event.date.toDate,
            Event.Type.id(event.eventtype),
            event.maybeEventId)
    })
  )  

  def postNewEvent() = silhouette.SecuredAction.async { implicit request =>
    eventForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(Json.obj("errors" -> form.errors.map{_.messages.mkString(", ")}))),
      event => {
        val maybeParticipantsCloned = event.maybeEventId match {
          case None => Future.successful(Nil)
          case Some(id) => participantDAO.find(id)
        }
        maybeParticipantsCloned.flatMap { participants =>
          eventDAO.save(Event(creator = request.identity,
            name= event.name,
            date= event.date,
            eventType= event.eventtype)).flatMap { new_event =>

            val allParticipants = participants.filter(_.user.id != request.identity.id).map(p => p.copy(id=None, eventid=new_event.id.get)) :+
              Participant(eventid=new_event.id.get, user=request.identity, role=Participant.Role.Owner)

            participantDAO.insert(allParticipants).map { _ =>
              c.increment(name = "giftyou.events.events.created", tags=Seq("creator-id" + request.identity.id))
              Ok(Json.toJson(new_event))
            }
          }
        }
      }
    )
  }

  def getEvents = silhouette.SecuredAction.async { implicit request =>
    eventDAO.findByUser(request.identity).map { events =>
      Ok(Json.obj("events" -> events))
    }
  }

  def getEventWithDetails(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](participantDAO, eventid)).async { implicit request =>
    eventDAO.find(eventid).flatMap { case(event) =>
      event match {
        case Some(e) =>
          giftDAO.findByEventId(request.identity, e.id.get).flatMap { gifts =>
            participantDAO.find(e.id.get).flatMap {participants =>
              WithOwnerOf.IsOwnerOf(participantDAO, eventid, request.identity).map { isOwnerOf =>
                Ok(Json.obj("event" -> e,
                   "gifts" -> gifts.map{g =>
                     val toUser = g.gift.to match {
                       case Some(user) if user.id == request.identity.id => true
                       case _ => false
                     }
                     g.copy(gift=g.gift.copy(
                       from = toUser match {
                         case true => None
                         case _ => g.gift.from
                       },
                       status = toUser match {
                         case true => Gift.Status.New
                         case _ => g.gift.status
                       }))
                   }.sortBy(_.gift.creationDate).reverse,
                   "participants" -> participants))
              }
            }
          }
        case _ => Future.successful(NotFound)
      }
    }
  }

  def getEventParticipants(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](participantDAO, eventid)).async { implicit request =>
    eventDAO.find(eventid).flatMap { case(event) =>
      event match {
        case Some(e) =>
          giftDAO.findByEventId(request.identity, e.id.get).flatMap { gifts =>
            participantDAO.find(e.id.get).map {participants =>
              Ok(Json.obj("participants" -> participants))
            }
          }
        case _ => Future.successful(NotFound)
      }
    }
  }

  def eventWithUser(eventid: Long, userid: UUID) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](participantDAO, eventid)).async { implicit request =>
    userService.retrieveById(userid).flatMap { to =>
      eventDAO.find(eventid).flatMap { case(event) =>
        event match {
          case Some(e) =>
            giftDAO.findByEventId(request.identity, e.id.get).flatMap { gifts =>
              participantDAO.find(e.id.get).flatMap {participants =>
                WithOwnerOf.IsOwnerOf(participantDAO, eventid, request.identity).map { isOwnerOf =>
                  Ok(Json.toJson(e))
                }
              }
            }
          case _ => Future.successful(NotFound)
        }
      }
    }
  }

    /**
   * Delete an event.
   */
  def deleteEvent(eventid: Long) = silhouette.SecuredAction(WithCreatorOf[DefaultEnv#A](eventDAO, eventid)).async { implicit request =>
    eventDAO.find(eventid).map { event =>
      event match {
        case Some(_) => {
          eventDAO.delete(eventid)
          Ok
        }
        case _ => BadRequest
      }
    }
  }
  
  
  val giftForm = Form[GiftSimple] {
    tuple(
      "id" -> optional(longNumber)
      ,"creatorid" -> nonEmptyText
      ,"eventid" -> longNumber
      ,"name" -> nonEmptyText
      ,"urls" -> list(text)
      ,"to" -> optional(nonEmptyText)
    ).transform(
    {/*apply*/
      case (optid, creatorid, eventid, name, urls, toid) => {        
        
        val pkid = optid

        val toid_uuid = toid.collect{case(id) => UUID.fromString(id)}
        GiftSimple(
            id = pkid,
            creatorid = UUID.fromString(creatorid),
            eventid=eventid,
            name=name, 
            urls=urls.filter(!_.isEmpty),
            toid = toid_uuid
            )
      }
    },{ /*unapply*/
      gift: GiftSimple => {
        val toid = gift.toid.collect{case(id) => id.toString}
        ( gift.id,
          gift.creatorid.toString,
          gift.eventid,
          gift.name,
          gift.urls,
          toid)
      }
    })
  }

  def postEditGift(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](participantDAO, eventid)).async { implicit request =>
    giftForm.bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(Json.obj("errors" -> form.errors.map{_.messages.mkString(", ")})))
      },
      gift => {
        
        // quickwin
        val to = gift.toid match {
          case Some(id) => userService.retrieveById(id)
          case None => Future[Option[User]](None)
        }
        val from = gift.fromid match {
          case Some(id) => userService.retrieveById(id)
          case None => Future[Option[User]](None)
        }

        val creator = userService.retrieveById(gift.creatorid)

        Future.sequence(List(to, from, creator)).flatMap { s =>
          val new_gift = Gift(
            id = gift.id,
            creator = s(2).get,
            eventid = gift.eventid,
            creationDate = gift.creationDate,
            name = gift.name,
            status = gift.status,
            to = s(0),
            from = s(1),
            urls = gift.urls)

          val newGift = gift.id match {
            case Some(id) => {
              historyDAO.save(
                History(
                  objectid=id,
                  user=request.identity,
                  category="Gift",
                  content="Update gift from " + gift + " to " +  new_gift)).flatMap { _ =>
                giftDAO.save(new_gift)
              }
            }
            case None => {
              c.increment(name = "giftyou.events.gifts.created", tags=Seq("creator-id:" + s(2).get.id))
              giftDAO.save(new_gift)
            }
          }
          newGift.map { gift =>
            eventNotificationService.publishGift(gift)
            Ok(Json.toJson(gift))
          }
        }
      }
    )
  }  
  
  def editGift(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    giftDAO.find(giftid).flatMap { case(maybeGift) =>
       maybeGift match {
         case Some(gift) =>
           val gift_simple = GiftSimple(
             id = gift.id,
             creatorid = gift.creator.id,
             eventid = gift.eventid,
             creationDate = gift.creationDate,
             name = gift.name,
             status = gift.status,
             toid = gift.to.map{_.id},
             fromid = gift.from.map{_.id},
             urls = gift.urls)

           eventDAO.find(gift.eventid).flatMap { case (maybeEvent) =>
             maybeEvent match {
               case Some(event) =>
                 participantDAO.find(gift.eventid).map { _ =>
                   Ok(Json.toJson(event))
                 }
               case _ => Future.successful(NotFound)
             }
           }

         case _ =>
           Future.successful(NotFound)
       }
    }
  }

  def getGift(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    giftDAO.find(giftid).map { maybeGift =>
      maybeGift match {
        case Some(gift) =>
          val toUser = gift.to match {
            case Some(user) if user.id == request.identity.id => true
            case _ => false
          }

          Ok(Json.toJson(gift.copy(
            from = toUser match {
              case true => None
              case _ => gift.from
            })))
        case _ =>
          NotFound
      }
    }
  }
  
  def getGiftWithDetails(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    giftDAO.find(giftid).flatMap { maybeGift =>
      maybeGift match {
        case Some(gift) =>
          historyDAO.findByCategoryAndId("Gift", gift.id.get).map { history =>
            val toUser = gift.to match {
              case Some(user) if user.id == request.identity.id => true
              case _ => false
            }

            val maybeHistory = toUser match {
              case true => List.empty
              case _ => history
            }

            Ok(Json.obj("gift" -> gift.copy(
              from = toUser match {
                case true => None
                case _ => gift.from
              },
              status = toUser match {
                case true => Gift.Status.New
                case _ => gift.status
              }),
              "history" -> maybeHistory))
          }
        case _ =>
          Future.successful(NotFound)
      }
    }
  }
  
  /**
   * Delete a gift.
   */
  def deleteGift(eventid: Long, giftid: Long) = silhouette.SecuredAction(WithGiftCreatorOf[DefaultEnv#A](giftDAO, giftid)).async { implicit request =>
    giftDAO.find(giftid).map { maybeGift =>
      maybeGift match {
        case Some(_) => {
          giftDAO.delete(giftid)
          c.increment(name = "giftyou.events.gifts.deleted")
          Ok
        }
        case None => BadRequest
      }
    }
  }
  
  
  def addParticipant(eventid: Long) = silhouette.SecuredAction.async { implicit request =>

    EventsController.addParticipantForm.bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(Json.obj("errors" -> form.errors.map{_.messages.mkString(", ")})))
      },
      tuple => {
        
        val email = tuple._1
        val role = Participant.Role.withName(tuple._2)

        userService.retrieveByEmail(email).flatMap {
          user => user match {
            case Some(u) =>  {
              participantDAO.find(eventid, u).flatMap { maybeParticipant =>
                val participant = maybeParticipant match {
                  case Some(p) => p.copy(role = role)
                  case None => Participant(
                    user = u,
                    eventid = eventid,
                    role = role)
                }
                participantDAO.save(participant).map { p =>
                  Ok(Json.toJson(p))
                }
              }
            }
            case None => {
              Future.successful(BadRequest("User with his email not found"))
            }
          }
        }
      }
    )
  }

  def getGiftComments(eventid: Long, giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    commentDAO.findByCategoryAndId(Comment.Category.Gift, giftid).map { comments =>
      // remove notifications for this user, he is going to read those messages !
      notificationDAO.delete(request.identity, Notification.Category.GiftComment, giftid)
      Ok(Json.obj("comments" -> JsArray(comments.map{p => Json.toJson(p)})))
    }
  }

  def postGiftComment(eventid: Long, giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    Form("comment" -> nonEmptyText).bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(Json.obj("errors" -> form.errors.map{_.messages.mkString(", ")})))
      },
      comment => {
        val commentObj = Comment(
          objectid=giftid,
          user=request.identity,
          category=Comment.Category.Gift,
          content=comment)
        commentDAO.save(commentObj).flatMap { _ =>
          eventNotificationService.publishComment(eventid, commentObj)
          commentDAO.findByCategoryAndId(Comment.Category.Gift, giftid).map { comments =>
            c.increment(name = "giftyou.events.gifts.comments", tags=Seq("gift-id:" + giftid, "user-id" + request.identity.id))
            Ok(Json.obj("comments" -> JsArray(comments.map{p => Json.toJson(p)})))
          }
        }
      }
    )
  }
  
  def updateGiftStatus(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    Form("status" -> nonEmptyText).bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(Json.obj("errors" -> form.errors.map{_.messages.mkString(", ")})))
      },
      status => {
        giftDAO.find(giftid).flatMap { maybeGift =>
          maybeGift match {
            case Some(gift) => {

              gift.from match {
                case Some(x) if x != request.identity => Future.successful(BadRequest)
                case _ => {
                  val statusValue = Gift.Status.withName(status)

                  val from = statusValue match {
                    case Gift.Status.New => None
                    case _ => Some(request.identity)
                  }

                  c.increment(name = "giftyou.events.gifts.updated", tags=Seq("gift-id:" + giftid, "status:" + statusValue))

                  historyDAO.save(
                    History(objectid = giftid,
                      user = request.identity,
                      category = "Gift",
                      content = "Update gift status from " + gift.status + " to " + status)
                  )

                  giftDAO.save(gift.copy(status=statusValue, from=from)).map { newGift =>
                    eventNotificationService.publishGift(newGift)
                    Ok(Json.toJson(newGift))
                  }
                }
              }
            }
            case None => Future.successful(BadRequest)
          }
        }
      }
    )
  }

  def eventWs(eventid: Long) =
    WebSocket.acceptOrResult[String, String] {
      case rh if sameOriginCheck(rh) =>
        implicit val req = Request(rh, AnyContentAsEmpty)
        silhouette.SecuredRequestHandler { securedRequest =>
          Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
        }.flatMap {
          case HandlerResult(r, Some(user)) =>
            eventDAO.find(eventid).map {
              case maybeEvent =>
                maybeEvent match {
                  case Some(event) =>
                    Right(ActorFlow.actorRef(out => EventActor.props(event, user, out, eventDAO)))
                  case _ => Left(r)
                }
            }
          case HandlerResult(r, None) => Future.successful(Left(r))
        }

      case rejected =>
        Logger.error(s"Request ${rejected} failed same origin check")
        Future.successful {
          Left(Forbidden("forbidden"))
        }
    }

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  private def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
    Logger.debug("Checking the ORIGIN ")

    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        Logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        Logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
        Logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URL(origin)
      (url.getHost == "localhost" || url.getHost() == "giftyou.herokuapp.com") &&
        (url.getPort match { case 9000 | 9001 | -1 | 19001 => true; case _ => false })
    } catch {
      case _: Exception => false
    }
  }
}

object EventsController {
    val addParticipantForm = Form {
    tuple(
      "email" -> nonEmptyText
      ,"role" -> nonEmptyText
    )
  }
}
