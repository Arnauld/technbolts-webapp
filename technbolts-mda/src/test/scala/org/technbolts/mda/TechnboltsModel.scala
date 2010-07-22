package org.technbolts.mda

import org.junit.Test
import annotation._
import java.util.Date
import protobuf.{ProtobufFile, ProtobufFieldType, ProtobufMessage, ProtobufField}

class TechnboltsModelTest {
  @Test
  def useCaseEx1: Unit = {
    val model = GeneratorModel("org.technbolts")
    model.models.append(
      classOf[ProtobufFileEntity],
      classOf[ProtobufFileCommon],
      classOf[ProtobufFileRequest],

      classOf[Ref],
      classOf[Request],
      classOf[RequestDetails],
      classOf[RequestEvent],
      classOf[Entity])
    model.generate
  }
}

@ProtobufFile(name="common", javaOuterClassName="CommonDTO")
class ProtobufFileCommon

@ValueObject
class RequestSystemState

@ValueObject
class RequestState

@ValueObject
class DistributionMode

@ValueObject
class RefType

@ValueObject
@ProtobufMessage(partOf="common")
sealed abstract class Ref(val refUuid: String)
case class UserRef (override val refUuid: String) extends Ref(refUuid)
case class GroupRef(override val refUuid: String) extends Ref(refUuid)


@ProtobufFile(name="request", javaOuterClassName="RequestDTO")
class ProtobufFileRequest

@ProtobufMessage(partOf = "request")
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

@ProtobufMessage(partOf = "request")
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

@ProtobufMessage(partOf = "request")
@ValueObject
class RequestEvent {
  @Id
  @ProtobufField
  var id: Int = _

  @ProtobufField(fieldType = ProtobufFieldType.Int)
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

@ProtobufFile(name="entity", javaOuterClassName="EntityDTO")
class ProtobufFileEntity

@ProtobufMessage(partOf = "entity")
@DomainModel
class Entity
