package org.technbolts.mda.protobuf

import org.technbolts.mda.Template

object ProtobufTemplate {
  def apply() = { new ProtobufTemplate }

  /**
   *
   */
  def generateProtoFile(model: ProtobufFileModel): String = {
    val builder = new StringBuilder
    new ProtobufTemplate().generateProtoFile(builder, model)
    builder.toString
  }
}

class ProtobufTemplate extends Template {

  var protoFileHeaderTemplate = """|package: ${protoPackage};
                                   |option java_package = "${java_package}";
                                   |option java_outer_classname = "${java_outer_classname}";
                                   |""".stripMargin

  var importTemplate = """|import "${import}";
                          |""".stripMargin

  var optimizeForSpeed = """|option optimize_for = SPEED;
                            |""".stripMargin

  val fieldTemplate = """|  ${classifier} ${type} ${name} = ${ordinal} ${default};
                         |""".stripMargin
  /**
   * 
   */
  def generateProtoFile(builder:StringBuilder, model: ProtobufFileModel): Unit = {
    val header = subVars(protoFileHeaderTemplate, Map(
                    "protoPackage"->model.protoPackage,
                    "java_package"->model.javaPackage,
                    "java_outer_classname"->model.javaOuterClassName))

    builder.append(header)
    if (model.optimizedForSpeed)
      builder.append(optimizeForSpeed);

    model.imports.foreach {imp =>
            val fragment = subVars(importTemplate, Map("import"->imp))
            builder.append(fragment)}
    
    model.messages.foreach {message => generateMessage(builder, message)}
  }

  /**
   *
   */
  def generateMessage(builder: StringBuilder, message: ProtobufMessageModel): Unit = {
    builder.append("message ").append(message.name).append(" {").append(NL);
    message.fields.foreach {field => generateField(builder, field)}
    builder.append("}").append(NL);
  }

  /**
   *
   */
  def generateField(builder: StringBuilder, field: ProtobufFieldModel): Unit = {
    val defaultValue = field.defaultValue match {
      case None => ""
      case Some(v) => "[default="+v+"]"
    }
    val fragment = subVars(fieldTemplate, Map(
                      "classifier"->field.classifier.pbuf,
                      "type"->field.fieldType.pbuf,
                      "name"->field.name,
                      "ordinal"->field.ordinal.getOrElse(-1).toString,
                      "default"->defaultValue
                    ))
    builder.append(fragment)
  }
}