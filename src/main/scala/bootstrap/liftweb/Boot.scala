package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import org.slf4j.{Logger, LoggerFactory}
import org.technbolts.model.User
import provider.HTTPRequest
import net.liftweb.mapper.{Schemifier, DefaultConnectionIdentifier, StandardDBVendor, DB}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  private val logger: Logger = LoggerFactory.getLogger(classOf[Boot])

  def boot {

    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(
        Props.get("db.driver") openOr "org.h2.Driver", //
        Props.get("db.url") openOr "jdbc:h2:db/technbolts.db;AUTO_SERVER=TRUE", //
        Props.get("db.user"), //
        Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // where to search snippet
    LiftRules.addToPackages("org.technbolts")

    Schemifier.schemify(true, Schemifier.infoF _, User)

    LiftSession.onSetupSession = List((s: LiftSession) => {
      logger.info("Session setup: " + s)
    })

    S.addAround(new LoanWrapper { // Y
      def apply[T](f: => T): T = {
        User.currentUser match {
          case Full(user) => MDC.put(("user", user.email))
          case _ => MDC.put(("user", "anonymous"))
        }
        val result = f // Let Lift do normal request processing.
        result
      }
    })

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: User.sitemap
    LiftRules.setSiteMap(SiteMap(entries: _*))

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    /*
     * Force the request to be UTF-8
     */
    LiftRules.early.append(makeUtf8)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}

