package org.technbolts.mda

import misc.{NamedStringModel, PagingModel, ErrorModel}
import org.junit.Test
import domain._
import org.scalatest.junit.JUnitSuite
import java.util.Date
import org.springframework.core.`type`.filter.AnnotationTypeFilter
import org.technbolts.reflect.ClassScanner
import ClassScanner._
import protobuf._

class TechnboltsModelSpec extends JUnitSuite {
  @Test
  def useCaseEx1: Unit = {
    
    val model = GeneratorModel("org.technbolts")

    val scanner = new ClassScanner
    scanner.addIncludeFilter(composeAnnotationsOr(classOf[ProtobufMessage], classOf[ProtobufFile]))
    scanner.getComponentClasses("org.technbolts").foreach {
      klazz =>
        println(klazz)
        model.models.append(klazz)
    }
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
@ProtobufMessage(partOf="common", name="Ref")
sealed abstract class Ref(val refUuid: String)
case class UserRef (override val refUuid: String) extends Ref(refUuid)
case class GroupRef(override val refUuid: String) extends Ref(refUuid)

@ValueObject
@ErrorModel
@ProtobufMessage(partOf="common", name="Error")
class Err

@ValueObject
@PagingModel
@ProtobufMessage(partOf="common", name="Paging")
class Paging

@ValueObject
@NamedStringModel
@ProtobufMessage(partOf="common", name="NamedString")
class NamedString {
  @ProtobufField
  var name: String = _;
  @ProtobufField
  var value:String = _;
}

@ProtobufInlined(fieldType=ProtobufFieldType.String)
class UUID

@ProtobufFile(name="request", javaOuterClassName="RequestDTO")
class ProtobufFileRequest

@ProtobufMessage(partOf = "request")
@DomainModel
class Request {
  @ProtobufField
  @Id
  var id: UUID = _;

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
  var id: UUID = _

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
class Entity {
  @Id
  @ProtobufField
  var id: UUID = _
}
