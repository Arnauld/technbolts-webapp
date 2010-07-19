package org.technbolts.itest.api

import org.technbolts.itest.HttpClientHelper
import java.io.ByteArrayOutputStream
import org.slf4j.{Logger, LoggerFactory}
import org.technbolts.service.api.ApiService
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.PutMethod
import org.junit.{Test, Before}
import org.technbolts.protobuf.{UserPBModel, UserPBCommand}
import UserPBModel._
import UserPBCommand._

class UserApiServiceIT extends HttpClientHelper {
   private val logger:Logger = LoggerFactory.getLogger(classOf[EntityApiServiceIT])

  var byteStream = new ByteArrayOutputStream

  @Before
  def setUp: Unit = {
  }

  @Test
  def createCase: Unit = {
    val user = User.newBuilder.setDetails(
        UserDetails.newBuilder.setEmail("robin.le.lapin@anim.aux")
                              .setFirstname("Robin")
                              .setLastname("Le Lapin")
      ).build

    val command = CreateUser.newBuilder.addUser(user).build
    command.writeTo(byteStream)

    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/user/create", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_OK)

    var response = CreateUserResponse.parseFrom(method.getResponseBodyAsStream)
    println("response="+response)
  }

  @Test
  def deleteCase: Unit = {
    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/user/delete", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_OK)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }

  @Test
  def searchCase: Unit = {
    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/user/search", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_OK)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }

  @Test
  def unknownCase: Unit = {
    // build content
    val raw = byteStream.toByteArray

    // Execute the method.
    val (statusCode,method:PutMethod) = httpPut("/api/user/invalid", raw, ApiService.ContentType)
    assertStatusCode(method, statusCode, HttpStatus.SC_NOT_ACCEPTABLE)

    val response = method.getResponseBodyAsString
    println("response="+response)
  }
}