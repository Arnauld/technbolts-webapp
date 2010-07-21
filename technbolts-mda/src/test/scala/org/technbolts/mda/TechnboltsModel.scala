package org.technbolts.mda

import org.junit.Test
import annotation._
import protobuf.{ProtobufFieldType, ProtobufMessage, ProtobufField}
import java.util.Date

class TechnboltsModelTest {
  @Test
  def useCaseEx1: Unit = {
    val model = GeneratorModel("org.technbolts")
    model.models.append(
      classOf[Request],
      classOf[RequestDetails],
      classOf[Entity])
    model.generate
  }
}

@ValueObject
class RequestSystemState

@ValueObject
class RequestState

@ValueObject
class DistributionMode

@ValueObject
class RefType

@ValueObject
sealed abstract class Ref {
  val refUuid: String
}
case class UserRef(val refUuid: String) extends Ref
case class GroupRef(val refUuid: String) extends Ref

@ProtobufMessage(message = "request")
@DomainModel
class Request {
  @ProtobufField
  @Id
  var uuid: String = _;

  @ProtobufField(fieldType = ProtobufFieldType.Int)
  var system_state: RequestSystemState = _

  @ProtobufField(fieldType = ProtobufFieldType.Int)
  var request_state: RequestState = _

  @ProtobufField
  var creationDate: Date = _

  @ProtobufField
  var lastUpdateDate: Date = _

  @ProtobufField
  var closeDate: Date = _

  @ProtobufField(fieldType = ProtobufFieldType.Int)
  var distributionMode: DistributionMode = _

  @ProtobufField
  var responsible: Ref = _;

  @ProtobufField
  var details: RequestDetails = _
}

@ProtobufMessage(message = "RequestDetails")
@ValueObject
class RequestDetails {
  @ProtobufField
  var locale: String = _

  @ProtobufField
  var created_by: Ref = _

  @ProtobufField
  var originator: Ref = _

  @ProtobufField
  var last_updated_by: Ref = _

  @ProtobufField
  var closed_by: Ref = _

  @ProtobufField
  var event: Iterable[RequestEvent] = _

  @ProtobufField
  var entity: Entity = _
}

@ProtobufMessage(message = "Entity")
@DomainModel
class Entity

@ProtobufMessage(message = "RequestEvent")
@ValueObject
class RequestEvent {
  @Id
  @ProtobufField
  var id: Int = _

  @ProtobufField
  var eventType: EventType = _

  @ProtobufField
  var eventDate: Date = _

  @ProtobufField
  var originated_by: Ref = _

  @ProtobufField
  var destination: Ref = _

  @ProtobufField
  var comment: String = _

  @ProtobufField
  var entity: Entity = _
}

class EventType