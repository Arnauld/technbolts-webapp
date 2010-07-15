package org.technbolts.itest

import org.mortbay.jetty.Server
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp.WebAppContext
import org.slf4j.LoggerFactory

trait JettyEmbedded {
  private val logger = LoggerFactory.getLogger(classOf[JettyEmbedded])

  val server = new Server
  var port = 8082

  def startServer = {
    val scc = new SelectChannelConnector
    scc.setPort(port)
    server.setConnectors(Array(scc))

    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setWar("src/main/webapp")

    server.addHandler(context)

    logger.info(">>> STARTING EMBEDDED JETTY SERVER")
    server.start()
  }

  def stopServer = {
    logger.info(">>> STOPING EMBEDDED JETTY SERVER")
    server.stop()
    server.join()
  }
}

class JettyEmbeddedImpl extends JettyEmbedded