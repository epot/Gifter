package controllers

import javax.inject.Inject
import java.util.UUID

import models.gift._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, MessagesApi}
import com.mohiva.play.silhouette.api.Silhouette
import models.daos._
import models.gift.Comment.CommentSimple
import models.services.UserService
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import utils.auth._
import com.github.nscala_time.time.Imports._

import scala.concurrent.Future

class Events @Inject() (
   val messagesApi: MessagesApi,
   userService: UserService,
   eventDAO: EventDAO,
   commentDAO: CommentDAO,
   participantDAO: ParticipantDAO,
   notificationDAO: NotificationDAO,
   giftDAO: GiftDAO,
   historyDAO: HistoryDAO,
   silhouette: Silhouette[DefaultEnv])
  extends Controller with I18nSupport {

  val eventForm = Form[EventSimple](
    tuple(
      "name" -> nonEmptyText
      ,"dateStr" -> date("dd-MM-yyyy")
      ,"type" -> nonEmptyText
      ).transform(
    {/*apply*/
      case (name, dateStr, eventtype) => {
        EventSimple(name=name, date=new DateTime(dateStr), eventtype=Event.Type.withName(eventtype))
      }
    },{ /*unapply*/
      event: EventSimple => (
            event.name,
            event.date.toDate,
            event.eventtype.toString)
    })
  )  
  
  def newEvent = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.newEvent(request.identity, eventForm)))
  }


  def postNewEvent() = silhouette.SecuredAction.async { implicit request =>
    eventForm.bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest(views.html.newEvent(request.identity, errors)))
      },
      event => {
         eventDAO.save(Event(creator = request.identity,
          name= event.name,
          date= event.date,
          eventType= event.eventtype)).flatMap { new_event =>

           participantDAO.save(
             Participant(eventid=new_event.id.get, user=request.identity, role=Participant.Role.Owner)).map { p =>
             Redirect(routes.HomeController.index)
           }
        }
      }
    )
  }  
  
  def event(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](participantDAO, eventid)).async { implicit request =>
    eventDAO.find(eventid).flatMap { case(event) =>
      event match {
        case Some(e) =>
          giftDAO.findByEventId(request.identity, e.id.get).flatMap { gifts =>
            participantDAO.find(e.id.get).flatMap {participants =>
              WithOwnerOf.IsOwnerOf(participantDAO, eventid, request.identity).map { isOwnerOf =>
                Ok(views.html.event(request.identity, e, gifts.sortBy(g => g.gift.creationDate).reverse, participants, isOwnerOf))
              }
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
                  Ok(views.html.event(request.identity, e, gifts.sortBy(g => g.gift.creationDate).reverse, participants, isOwnerOf, to))
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
  def postDeleteEvent(eventid: Long) = silhouette.SecuredAction(WithCreatorOf[DefaultEnv#A](eventDAO, eventid)).async { implicit request =>
    eventDAO.find(eventid).map { event =>
      event match {
        case Some(e) => {
          eventDAO.delete(eventid)
          Redirect(routes.HomeController.index)
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
      ,"urls" -> list(nonEmptyText)
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
            urls=urls,
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

  def newGift(eventid: Long) = silhouette.SecuredAction.async { implicit request =>
    eventDAO.find(eventid).flatMap { case (maybeEvent) =>
      maybeEvent match {
        case Some(event) =>
          participantDAO.find(eventid).map { participants =>
            Ok(views.html.gifts.edit_gift(
              request.identity,
              giftForm.fill(GiftSimple(creatorid=request.identity.id, eventid=eventid, name="")),
              event,
              participants))
          }
        case _ => Future.successful(NotFound)
      }
    }
  }

  def postEditGift(eventid: Long) = silhouette.SecuredAction(WithParticipantOf[DefaultEnv#A](participantDAO, eventid)).async { implicit request =>
    giftForm.bindFromRequest.fold(
      errors => {
        println(errors)
        eventDAO.find(eventid).flatMap { case (maybeEvent) =>
          maybeEvent match {
            case Some(event) =>
              participantDAO.find(eventid).map { participants =>
                BadRequest(views.html.gifts.edit_gift(
                  request.identity,
                  errors,
                  event,
                  participants))
              }
            case _ => Future.successful(NotFound)
          }
        }
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
            case None => giftDAO.save(new_gift)
          }
          newGift.map { gift =>
            gift.to match {
              case Some(user_to) => Redirect(routes.Events.eventWithUser(gift.eventid, user_to.id))
              case _ => Redirect(routes.Events.event(gift.eventid))
            }
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
                 participantDAO.find(gift.eventid).map { participants =>
                   Ok(views.html.gifts.edit_gift(request.identity, giftForm.fill(gift_simple), event, participants))
                 }
               case _ => Future.successful(NotFound)
             }
           }

         case _ =>
           Future.successful(NotFound)
       }
    }
  }
  
  def viewGift(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    giftDAO.find(giftid).flatMap { maybeGift =>
      maybeGift match {
        case Some(gift) =>
          historyDAO.findByCategoryAndId("Gift", gift.id.get).map { history =>
            Ok(views.html.gifts.view_gift(request.identity, gift, history))
          }
        case _ =>
          Future.successful(NotFound)
      }
    }
  }
  
  /**
   * Delete a gift.
   */
  def postDeleteGift(giftid: Long) = silhouette.SecuredAction(WithGiftCreatorOf[DefaultEnv#A](giftDAO, giftid)).async { implicit request =>
    giftDAO.find(giftid).map { maybeGift =>
      maybeGift match {
        case Some(gift) => {
          giftDAO.delete(giftid)
          Redirect(routes.Events.event(gift.eventid))
        }
        case None => BadRequest
      }
    }
  }
  
  
  def addParticipant = silhouette.SecuredAction.async { implicit request =>

    Events.addParticipantForm.bindFromRequest.fold(
      errors => {
        Future.successful(BadRequest(views.html.participants.participants_add_form(errors)))
      },
      tuple => {
        
        val eventid = tuple._1
        val email = tuple._2
        val role = Participant.Role.withName(tuple._3)

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
                participantDAO.save(participant).flatMap { _ =>
                  participantDAO.find(eventid).flatMap { participants =>
                    eventDAO.find(eventid).flatMap { maybeEvent =>
                      maybeEvent match {
                        case Some(event) =>
                          WithOwnerOf.IsOwnerOf(participantDAO, eventid, request.identity).map { isOwnerOf =>
                            Ok(views.html.participants.participants_table(
                              request.identity,
                              event,
                              participants,
                              isOwnerOf)
                            )

                          }

                        case _ => Future.successful(NotFound)
                      }
                    }
                  }
                }
              }
            }
            case None => {
              val form = Events.addParticipantForm.fill(tuple)
              Future.successful(BadRequest(
                views.html.participants.participants_add_form(
                  form.withError("email","User with his email not found")))
              )
            }
          }
        }
      }
    )
  }

  def getGiftComments(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    commentDAO.findByCategoryAndId(Comment.Category.Gift, giftid).map { comments =>
      // remove notifications for this user, he is going to read those messages !
      notificationDAO.delete(request.identity, Notification.Category.GiftComment, giftid)
      val json = Json.toJson(for(c <- comments) yield {CommentSimple(c.content, c.user.userName, c.creationDate)})
      Ok(json)
    }
  }

  def postGiftComment(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    Form("comment" -> nonEmptyText).bindFromRequest.fold(
      errors => {
        println(errors)
        Future.successful(BadRequest)
      },
      comment => {
        commentDAO.save(Comment(
          objectid=giftid,
          user=request.identity,
          category=Comment.Category.Gift,
          content=comment)).flatMap { _ =>
          commentDAO.findByCategoryAndId(Comment.Category.Gift, giftid).map { comments =>
            val json = Json.toJson(for(c <- comments) yield {CommentSimple(c.content, c.user.userName, c.creationDate)})
            Ok(json)
          }
        }
      }
    )
  }
  
  def updateGiftStatus(giftid: Long) = silhouette.SecuredAction(WithParticipantOfWithGift[DefaultEnv#A](giftDAO, participantDAO, giftid)).async { implicit request =>
    Form("status" -> nonEmptyText).bindFromRequest.fold(
      errors => {
        Future.successful(BadRequest)
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

                  historyDAO.save(
                    History(objectid = giftid,
                      user = request.identity,
                      category = "Gift",
                      content = "Update gift status from " + gift.status + " to " + status)
                  )

                  giftDAO.save(gift.copy(status=statusValue, from=from)).map { newGift =>
                    newGift.to match {
                      case Some(user_to) => {
                        Redirect(routes.Events.eventWithUser(
                          newGift.eventid, user_to.id))
                      }
                      case _ =>
                        Redirect(routes.Events.event(newGift.eventid))
                    }
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
}

object Events {
    val addParticipantForm = Form {
    tuple(
      "eventid" -> longNumber
      ,"email" -> nonEmptyText
      ,"role" -> nonEmptyText
    )
  }
}
