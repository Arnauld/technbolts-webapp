package org.technbolts.itest

import org.junit.runner.RunWith
import org.junit.{AfterClass, BeforeClass}

import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(classOf[Suite])
@SuiteClasses(Array(classOf[SimpleIT]))
object JettyEmbeddedTest {
  val jetty = new JettyEmbeddedImpl
  
  @BeforeClass
  def setUp:Unit = jetty.startServer

  @AfterClass
  def tearDown:Unit = jetty.stopServer
}