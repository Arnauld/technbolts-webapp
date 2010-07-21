package org.technbolts.mda.protobuf

import org.technbolts.mda.Template

object ProtobufTemplate {
  def apply() = { new ProtobufTemplate }

  /**
   *
   */
  def generateProtoFile(model: ProtobufModel): String = {
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

  /**
   * 
   */
  def generateProtoFile(builder:StringBuilder, model: ProtobufModel): Unit = {
    val header = subVars(protoFileHeaderTemplate,
                         Map("protoPackage"->model.protoPackage,
                             "java_package"->model.javaPackage,
                             "java_outer_classname"->model.javaOuterClassName))

    builder.append(header)
    if (model.optimizeForSpeed)
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
    val template = """|From: ${FROM}
                      |To: ${TO}
                      |Subject: ${SUBJECT}
                      |
                      |Please, stop it.""".stripMargin
    builder.append(INDENT).append(field.classifier.pbuf).append(" ").append(field.fieldType.pbuf).append(" ").append(field.name).append(" = ").append(field.ordinal).append(NL);
  }
}