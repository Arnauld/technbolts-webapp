package org.technbolts.itest.api

import org.slf4j.LoggerFactory
import org.apache.commons.httpclient.{HttpStatus}
import org.apache.commons.httpclient.methods.{PutMethod}
import java.io.ByteArrayOutputStream
import org.junit.{Test, Before}
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers._
import org.technbolts.itest.HttpClientHelper
import org.technbolts.service.api.ApiService

class EntityApiServiceIT extends HttpClientHelper {
   private val logger = LoggerFactory.getLogger(classOf[EntityApiServiceIT])

  var byteStream = new ByteArrayOutputStream

  @Before
  def setUp: Unit = {
  }

  @Test
  def createCase: Unit = {
    // build content
    val raw = byteStream.toByteArray
    
    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/entity/create", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_OK)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }

  @Test
  def deleteCase: Unit = {
    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/entity/delete", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_OK)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }

  @Test
  def searchCase: Unit = {
    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/entity/search", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_OK)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }

  @Test
  def unknownCase: Unit = {
    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/entity/invalid", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_NOT_ACCEPTABLE)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }
}