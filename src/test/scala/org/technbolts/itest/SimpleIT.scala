package org.technbolts.itest

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.{Before, Test}

class SimpleIT {

  @Before
  def startServer = {
    
  }

  @Test
  def simpleCase:Unit = {
    val client = new HttpClient
    val method = new GetMethod("http://localhost:8082/")
    client.executeMethod(method)
    val response = method.getResponseBodyAsString
    println(response)
  }
}