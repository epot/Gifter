package models.gift

import java.util.UUID
import models.user._
import play.i18n.Messages

case class Participant(
  id: Option[Long] = None,
  user: User,
  eventid: Long,
  role: Participant.Role.Value)

object Participant {
  object Role extends Enumeration {
    val Owner = Value(1, Messages.get("participant.statut.owner"))
    val Gifter = Value(2, Messages.get("participant.statut.gifter"))
    val Reader = Value(3, Messages.get("participant.statut.reader"))
    class ParticipantVal(name: String, val x : String) extends Val(nextId, name)
    protected final def Value(name: String, x : String): ParticipantVal = new ParticipantVal(name, x)
  }

  case class BaseParticipant(
    id: Option[Long] = None,
    userid: UUID,
    eventid: Long,
    role: Int)

}
