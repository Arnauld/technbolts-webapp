package org.technbolts.mda.protobuf

import java.lang.reflect.Field
import collection.mutable.{HashSet, ListBuffer}

class ProtobufFileModel(val name:String) {
  var protoFileName:String = _
  var protoPackage:String = _
  var javaPackage:String = _
  var javaOuterClassName:String = _
  var optimizedForSpeed:Boolean = true
  var imports = new HashSet[String]
  var messages = new ListBuffer[ProtobufMessageModel]

  def addImport(required:String):Unit = {
    //no self dependency
    if(required!=protoFileName)
      imports.add(required)  
  }
}

class ProtobufMessageModel(var name:String, val partOf:String) {
  var fields = new ListBuffer[ProtobufFieldModel]

  def generateFieldOrdinals:Unit = {
    var max = fields.foldLeft(0) { (cur, field) => Math.max(cur, field.ordinal.getOrElse(-1)) }
    fields.foreach { field =>
      if(field.ordinal.isEmpty) {
        max = max+1;
        field.ordinal = Some(max)
      }
    }
  }

  def sortFields:Unit = {
    fields.sortWith((f1,f2) => f1.ordinal.getOrElse(-1) < f2.ordinal.getOrElse(-1))
  }

  var relatedClass:Option[Class[_]] = None
}

sealed abstract class ProtobufTypeModel(val pbuf:String)
case class ProtobufTypeAuto   extends ProtobufTypeModel("<not applicable>")
case class ProtobufTypeInt32  extends ProtobufTypeModel("int32")
case class ProtobufTypeInt64  extends ProtobufTypeModel("int64")
case class ProtobufTypeFloat  extends ProtobufTypeModel("float")
case class ProtobufTypeDouble extends ProtobufTypeModel("double")
case class ProtobufTypeBool   extends ProtobufTypeModel("bool")
case class ProtobufTypeString extends ProtobufTypeModel("string")
case class ProtobufTypeBytes  extends ProtobufTypeModel("bytes")
case class ProtobufTypeMessage(val message:ProtobufMessageModel) extends ProtobufTypeModel(message.name)

class ProtobufFieldModel(val name:String) {
  var ordinal:Option[Int] = None
  var classifier:ProtobufFieldClassifier = ProtobufFieldClassifier.Auto
  var fieldType:ProtobufTypeModel = ProtobufTypeAuto()
  var relatedField:Option[Field] = None
  var defaultValue:Option[String] = None

  def withClassifier(classifier:ProtobufFieldClassifier):ProtobufFieldModel = {
    this.classifier = classifier
    this
  }
  def optional:ProtobufFieldModel = withClassifier(ProtobufFieldClassifier.Optional)
  def repeated:ProtobufFieldModel = withClassifier(ProtobufFieldClassifier.Repeated)
  def required:ProtobufFieldModel = withClassifier(ProtobufFieldClassifier.Required)

  def withDefault(defaultValue:String):ProtobufFieldModel = {
    this.defaultValue = Some(defaultValue)
    this
  }
  def withFieldType(fieldType:ProtobufTypeModel):ProtobufFieldModel = {
    this.fieldType = fieldType
    this
  }
  def withFieldType(message:ProtobufMessageModel):ProtobufFieldModel = {
    this.fieldType = ProtobufTypeMessage(message)
    this
  }
}