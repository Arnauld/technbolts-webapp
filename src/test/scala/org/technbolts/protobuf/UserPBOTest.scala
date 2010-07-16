package org.technbolts.protobuf

import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import UserPBO._

class UserPBOTest {

  @Test
  def simpleCase:Unit = {
    val outstream = new ByteArrayOutputStream
    val details:UserDetails = UserDetails.newBuilder.setNickname("nabu").setEmail("nabu@kodono.zor").build
    val userWritten:User = User.newBuilder().setId(1).setDetails(details).build

    userWritten.writeTo(outstream)

    val asString = new StringBuilder
    outstream.toByteArray.foreach((b:Byte)=> asString.append(b).append(" "))
    println(asString)

    val instream = new ByteArrayInputStream(outstream.toByteArray)
    val user:User = User.parseFrom(instream)
    assertThat(user.getId, equalTo(1))
    assertThat(user.getDetails.getNickname, equalTo("nabu"))
    assertThat(user.getDetails.getEmail, equalTo("nabu@kodono.zor"))
  }
}