package utils

import java.util.TimeZone

import jdub.async.Database
import org.joda.time.DateTimeZone
import play.api.{ Application, GlobalSettings }
import services.database.Schema
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.Configuration.Default

object PlayGlobalSettings extends GlobalSettings {
  override def onStart(app: Application) = {
    DateTimeZone.setDefault(DateTimeZone.UTC)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    val cnf = play.api.Play.current.configuration
    val conf = URLParser.parse(cnf.getString("db.default.url").get)
  
    if (conf.username == Default.username) {
      Database.open(conf.copy(username= "epot"))
    }
    else {
      Database.open(conf)
    }
    Schema.update()

    super.onStart(app)
  }

  override def onStop(app: Application) = {
    Database.close()
    super.onStop(app)
  }
}
