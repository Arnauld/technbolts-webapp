package org.technbolts.mda.protobuf

import collection.mutable.ListBuffer
import java.lang.reflect.Field

/*

package org.technbolts.protobuf;

option java_package = "org.technbolts.protobuf";
option java_outer_classname = "RequestPBModel";

option optimize_for = SPEED;

import "common.proto";

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//   request
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
message Request {
    // system
    optional uint64 id = 1;
    optional uint32 system_state = 2;
    optional uint32 request_state = 3;

    optional int64  creation_date = 4;
    optional int64  last_update_date = 5;
    optional int64  close_date = 6;
    optional uint32 distribution_mode = 7;

    //
    optional Ref    responsible = 8;

    // details
    optional RequestDetails details = 10;
}


 */

class ProtobufModel(val name:String) {
  var protoFileName:String = _
  var protoPackage:String = _
  var javaPackage:String = _
  var javaOuterClassName:String = _
  var optimizeForSpeed:Boolean = true
  var imports = new ListBuffer[String]
  var messages = new ListBuffer[ProtobufMessageModel]
}

class ProtobufMessageModel(val name:String, val partOf:String) {
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
case class ProtobufTypeInt32  extends ProtobufTypeModel("int32")
case class ProtobufTypeInt64  extends ProtobufTypeModel("int64")
case class ProtobufTypeFloat  extends ProtobufTypeModel("float")
case class ProtobufTypeDouble extends ProtobufTypeModel("double")
case class ProtobufTypeString extends ProtobufTypeModel("string")
case class ProtobufTypeBytes  extends ProtobufTypeModel("bytes")
case class ProtobufTypeMessage(val message:String) extends ProtobufTypeModel(message)

class ProtobufFieldModel(val name:String) {
  var ordinal:Option[Int] = None
  var classifier:ProtobufFieldClassifier = ProtobufFieldClassifier.Auto
  var fieldType:Option[ProtobufTypeModel] = None
  var relatedField:Option[Field] = None
}