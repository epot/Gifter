import play.api.mvc._
import java.util.Date
import org.joda.time.DateTime
import play.api._
import play.api.db._
import play.api.Play.current
import anorm.Id

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("bootstrapping")
    bootstrap.InitialData.addTestData()
  }
}

package bootstrap {
  /** Initial set of data
   */
  object InitialData {
    import models.user._
    import models.gift._

    def addTestData() = {

      // Import initial data if the database is empty
      if (User.count == 0) {
        val usersWithIdentity = Map(
          (User(name = "Bob la fouine"), Identity(email="bob@gmail.com", adapter=Identity.Adapter.UserWithPassword, hash="secret")),
          (User(name = "Manu"), Identity(email="emmanuel.pot@gmail.com", adapter=Identity.Adapter.Google))
        )
        DB.withTransaction { implicit connection =>
          
          usersWithIdentity.foreach(tuple => User.create(tuple._1, tuple._2))
          
          val manu = User.findByEmail("emmanuel.pot@gmail.com").get
          
          Event.create(Event(creator=manu, name="Noel 2013"))
          Event.create(Event(creator=manu, name="Anniversaire nico 2013"))
          
          println("gnnaa")
          for(event <- Event.findByUser(manu)) {
            println(event)
          }
        }
      }
    }
  }
}