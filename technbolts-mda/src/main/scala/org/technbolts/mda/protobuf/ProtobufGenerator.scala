package org.technbolts.mda.protobuf

import org.technbolts.mda.{GeneratorModel, Generator}
import collection.mutable.HashMap
import org.technbolts.mda.annotation.{ProtobufField, ProtobufMessage, Protobuf}
import _root_.java.lang.reflect.Field
import org.technbolts.mda.model.{ProtobufFieldModel, ProtobufMessageModel, ProtobufModel}
import ProtobufField.Mode._
import org.slf4j.{LoggerFactory, Logger}

class ProtobufGenerator extends Generator {

  private val logger: Logger = LoggerFactory.getLogger(classOf[ProtobufGenerator])

  //
  var useClazzAsProtoPackage = false
  var protobufSubPackage:String = "protobuf"
  var protobufPackage:String = _
  //
  var useClazzAsProtoFileName = false
  //
  var useClazzAsJavaPackage = false
  var javaSubPackage:String = "protobuf"
  var javaPackage:String = _
  //
  var useClazzAsJavaOuterClassName = false
  //
  var fieldFormatter = (s:String)=> { s.replaceAll("[A-Z]", "_$0").toLowerCase }

  def getProtoPackageName(clazz:Class[_]):String = {
    if(useClazzAsProtoPackage)
      clazz.getPackage.getName
    else {
      if(protobufPackage==null)
        protobufPackage = generatorModel.rootPackage+"."+protobufSubPackage
      protobufPackage
    }
  }

  def getProtoFileName(clazz:Class[_]):String = {
    if(useClazzAsProtoFileName)
      clazz.getCanonicalName+".proto"
    else
      // what else to return ?
      clazz.getCanonicalName+".proto"
  }

  def getJavaPackageName(clazz:Class[_]):String = {
    if(useClazzAsJavaPackage)
      clazz.getPackage.getName
    else {
      if(javaPackage==null)
        javaPackage = generatorModel.rootPackage+"."+javaSubPackage
      javaPackage
    }
  }

  def getJavaOuterClassName(clazz:Class[_]):String = {
    if(useClazzAsJavaOuterClassName)
      clazz.getSimpleName
    else
      // what else to return ?
      clazz.getSimpleName
  }

  val protobufModels = new HashMap[String,ProtobufModel]

  /**
   *
   */
  def generate() = {
    logger.info("Generating protobuf files")

    logger.info(">Collecting models")
    // first collect models
    getModelsWithAnnotation(classOf[Protobuf]).foreach { clazz => processModel(clazz) }
    logger.info(">Models collected: "+protobufModels.size)

    logger.info(">Collecting messages")
    // then collect message to fill models
    getModelsWithAnnotation(classOf[ProtobufMessage]).foreach { clazz => processMessage(clazz) }

    val count = protobufModels.values.foldLeft(0) {
      (prev, model) => model.messages.size + prev
    }
    logger.info(">Messages collected: "+count)

    // resolve dependencies if any and required import
    resolveDependenciesAndFieldType();

    // then generate proto file
    logger.info(">Generating #"+protobufModels.size+" proto file(s)")
    protobufModels.values.foreach { model =>
        logger.info(">Generating proto file for model: "+model.name)
        val content = generateProtoFile(model)
        logger.debug(">Generated: "+model.protoFileName+"\n"+content)
    }
  }

  def resolveDependenciesAndFieldType:Unit = {
//    model.fieldType = aField.fieldType match {
//    }
  }

  /**
   *
   */
  def generateProtoFile(model:ProtobufModel):String = {

    val builder = new StringBuilder
    builder.append("package ").append(model.protoPackage).append(";").append(NL)
    builder.append("option java_package = \"").append(model.javaPackage).append("\";").append(NL)
    builder.append("option java_outer_classname = \"").append(model.javaOuterClassName).append("\";").append(NL)
    if(model.optimizeForSpeed)
      builder.append("option optimize_for = SPEED;").append(NL);
    model.imports.foreach { imp => builder.append ("import \""+imp+"\";").append(NL)}
    model.messages.foreach { message => generateMessage(builder, message) }
    builder.toString
  }

  /**
   *
   */
  def generateMessage(builder:StringBuilder, message:ProtobufMessageModel):Unit = {
    builder.append("message ").append(message.name).append(" {").append(NL);
    message.fields.foreach { field => generateField(builder, field) }
    builder.append("}").append(NL);
  }

  /**
   *
   */
  def generateField(builder:StringBuilder, field:ProtobufFieldModel):Unit = {
    builder.append(INDENT).append(field.mode).append(" ").append(field.fieldType).append(" ").append(field.name).append(" = ").append(field.ordinal).append(NL);
  }

  /**
   *
   */
  def processModel (clazz:Class[_]):Unit = {
    val protobuf:Protobuf = clazz.getAnnotation(classOf[Protobuf])

    protobufModels.get(protobuf.name) match {
      case Some(m) => throw new IllegalStateException("Duplicate Protobuf model with name <"+protobuf.name+">")
      case _ => {
        val m = new ProtobufModel(protobuf.name)
        m.protoPackage = defaultIfEmpty(protobuf.protoPackage, getProtoPackageName(clazz))
        m.protoFileName = defaultIfEmpty(protobuf.protoFileName, getProtoFileName(clazz))
        m.javaOuterClassName = defaultIfEmpty(protobuf.javaOuterClassName, getJavaOuterClassName(clazz))
        m.javaPackage = defaultIfEmpty(protobuf.javaPackage, getJavaPackageName(clazz))
        m.optimizeForSpeed = protobuf.optimizeForSpeed
        protobufModels.put(m.name, m)
      }
    }
  }

  /**
   *
   */
  def generateModelFor(clazz:Class[_], message:ProtobufMessage):ProtobufModel = {
    logger.info(">Generating model for message: "+message.message)

    val m = new ProtobufModel(message.message)
    m.protoPackage = getProtoPackageName(clazz)
    m.protoFileName = getProtoFileName(clazz)
    m.javaOuterClassName = getJavaOuterClassName(clazz)
    m.javaPackage = getJavaPackageName(clazz)
    m.optimizeForSpeed = true
    protobufModels.put(m.name, m)
    m
  }

  /**
   *
   */
  def processMessage(clazz:Class[_]):Unit = {
    val message:ProtobufMessage = clazz.getAnnotation(classOf[ProtobufMessage])
    val model = new ProtobufMessageModel(message.message, message.partOf)

    val fields = getFieldsWithAnnotation(clazz, classOf[ProtobufField])
    logger.info("Processing message #"+fields.size+" fields found");

    model.fields.appendAll(fields.map { field => processField(field) })
    model.generateFieldOrdinals
    model.sortFields

    val enclosing =
      if(message.partOf.isEmpty)
        generateModelFor(clazz, message)
      else {
        protobufModels.get(message.partOf) match {
          case Some(e) => e
          case _ => throw new IllegalStateException("Part of <"+message.partOf+"> value does not refer to a known model")
        }
      }
    enclosing.messages.append(model)
  }

  /**
   *
   */
  def getFieldName(field:Field) = fieldFormatter(field.getName)

  /**
   *
   */
  def processField(field:Field):ProtobufFieldModel = {
    val aField:ProtobufField = field.getAnnotation(classOf[ProtobufField])

    val model = new ProtobufFieldModel (defaultIfEmpty(aField.name, getFieldName(field)))
    if(aField.order>0)
      model.ordinal = aField.order
    model.mode = aField.mode.pbuf
    model
  }
}