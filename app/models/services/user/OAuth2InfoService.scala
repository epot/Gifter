package models.services.user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import jdub.async.Database
import models.queries.OAuth2InfoQueries
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class OAuth2InfoService extends DelegableAuthInfoDAO[OAuth2Info] {
  override def find(loginInfo: LoginInfo) = Database.query(OAuth2InfoQueries.getById(Seq(loginInfo.providerID, loginInfo.providerKey)))

  override def save(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    Database.transaction { conn =>
      Database.execute(OAuth2InfoQueries.UpdateOAuth2Info(loginInfo, authInfo), Some(conn)).flatMap { rowsAffected =>
        if (rowsAffected == 0) {
          Database.execute(OAuth2InfoQueries.CreateOAuth2Info(loginInfo, authInfo), Some(conn)).map(x => authInfo)
        } else {
          Future.successful(authInfo)
        }
      }
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    Database.execute(OAuth2InfoQueries.CreateOAuth2Info(loginInfo, authInfo)).map(x => authInfo)
  }

  override def update(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    Database.execute(OAuth2InfoQueries.UpdateOAuth2Info(loginInfo, authInfo)).map(x => authInfo)
  }

  override def remove(loginInfo: LoginInfo) = {
    Database.execute(OAuth2InfoQueries.removeById(Seq(loginInfo.providerID, loginInfo.providerKey))).map(x => Unit)
  }
}
