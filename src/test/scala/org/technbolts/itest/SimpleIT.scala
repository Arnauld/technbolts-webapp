package org.technbolts.itest

import org.apache.commons.httpclient.{HttpStatus, HttpClient}
import org.apache.commons.httpclient.methods.{ByteArrayRequestEntity, RequestEntity, PutMethod, GetMethod}
import org.slf4j.LoggerFactory
import org.junit.{Before, Assert, Test}
import org.technbolts.protobuf.UserPBModel._
import java.io.ByteArrayOutputStream
import org.technbolts.service.api.ApiService

class SimpleIT {
  private val logger = LoggerFactory.getLogger(classOf[SimpleIT])

  var details:UserDetails = _
  var user:User = _

  @Before
  def setUp: Unit = {
    details = UserDetails.newBuilder.setNickname("nabu").setEmail("nabu@kodono.zor").build
    user = User.newBuilder.setUuid("uuid1").setDetails(details).build
  }

  @Test
  def simpleCase: Unit = {
    val client = new HttpClient
    val method = new PutMethod("http://localhost:8082/api")

    val byteStream = new ByteArrayOutputStream
    user.writeTo(byteStream)

    // Request content will be retrieved directly
    // from the input-stream/bytes
    val raw = byteStream.toByteArray
    val entity:RequestEntity = new ByteArrayRequestEntity(raw, ApiService.ContentType);
    method.setRequestEntity(entity);

    // Execute the method.
    val statusCode = client.executeMethod(method);
    if (statusCode != HttpStatus.SC_OK) {
      Assert.fail("Method failed: " + method.getStatusLine());
    }

    val responseStream = method.getResponseBody
    var userReturned = User.parseFrom(responseStream)
    println("userReturned="+userReturned)
  }
}