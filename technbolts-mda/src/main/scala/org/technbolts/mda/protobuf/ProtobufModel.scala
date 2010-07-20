package org.technbolts.mda.model

import collection.mutable.ListBuffer
import org.technbolts.mda.annotation.ProtobufField
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
    var max = fields.foldLeft(0) { (cur, field) => Math.max(cur, field.ordinal) }
    fields.foreach { field =>
      if(field.ordinal<=0) {
        max = max+1;
        field.ordinal = max
      }
    }
  }

  def sortFields:Unit = {
    fields.sortWith((f1,f2) => f1.ordinal < f2.ordinal)
  }

  var relatedClass:Option[Class[_]] = None
}

class ProtobufFieldModel(val name:String) {
  var ordinal:Int = -1
  var mode:String = ProtobufField.Mode.Optional.pbuf
  var fieldType:Option[String] = None
  var relatedField:Option[Field] = None
}