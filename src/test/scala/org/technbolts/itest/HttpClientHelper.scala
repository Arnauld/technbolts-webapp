package org.technbolts.itest

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers._
import org.apache.commons.httpclient.{HttpMethod, HttpClient}
import org.apache.commons.httpclient.methods._
import java.io.{InputStream, File=>JFile}

trait HttpClientHelper {
  var rootUrl = "http://localhost:8082"
  val httpClient =  new HttpClient
  var charset = "UTF8"

  def assertStatusCode(method:HttpMethod, actualStatusCode:Int, expectedStatusCode:Int):Unit = {
    assertThat("Method failed: " + method.getStatusLine(), actualStatusCode, equalTo(expectedStatusCode))
  }

  def executeMethod(method:HttpMethod):Int = {
    // Execute the method.
    val statusCode = httpClient.executeMethod(method);
    statusCode
  }

  def httpPut(pathFragment:String, content:Any, contentType:String):(Int,PutMethod) = {
    val method = new PutMethod(rootUrl+pathFragment)
    val entity:RequestEntity = content match {
      case bytes:Array[Byte] => new ByteArrayRequestEntity(bytes, contentType)
      case stream:InputStream => new InputStreamRequestEntity(stream,contentType)
      case string:String => new StringRequestEntity(string,contentType,charset)
      case file:JFile => new FileRequestEntity(file,contentType)
      case _ => throw new IllegalArgumentException("content's type is not supported")
    }
    method.setRequestEntity(entity);

    val statusCode = executeMethod(method)
    (statusCode, method)
  }
}