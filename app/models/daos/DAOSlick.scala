package models.daos

import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

/**
 * Trait that contains generic slick db handling code to be mixed in with DAOs
 */
trait DAOSlick extends DBTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]