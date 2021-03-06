package org.technbolts.mda

import model.PrototypeModel
import protobuf.ProtobufGenerator

import collection.mutable.{ListBuffer, HashMap}
import _root_.java.lang.reflect.Field
import _root_.java.lang.annotation.{Annotation => JAnnotation }
import java.util.Date

object GeneratorModel {
  def apply(root:String) = {
    val model = new GeneratorModel {
      val rootPackage = root;
    }
    model
  }
}



trait GeneratorModel {

  val rootPackage:String
  val models = new ListBuffer[Class[_]]

  def getModelsWithAnnotation(annotation:Class[_ <: JAnnotation]):List[Class[_]] = {
    models.filter { p:Class[_] => p.isAnnotationPresent(annotation) } .toList
  }

  var generators:List[Generator] = null
  def getGenerators:List[Generator] = {
    if(generators==null) {
      val protobuf = new ProtobufGenerator
      protobuf.initDefaultPlugins
      generators = List(protobuf)
    }
    generators
  }

  //
  val prototypes = new HashMap[String,PrototypeModel]

  def generate:Unit = {
    getGenerators.foreach { g =>
      g.initialize(this)
      g.generate()
    }
  }
}

trait Generator {

  def generate():Unit

  def isEmpty(value:String):Boolean = (value==null || value.isEmpty)

  def defaultIfEmpty(value:String, default:String) = {
    if(isEmpty(value))
      default
    else
      value
  }

  var generatorModel: GeneratorModel = _

  /**
   *
   */
  def initialize(model:GeneratorModel):Unit = {
    this.generatorModel = model;
  }

  def getFieldsWithAnnotation(clazz:Class[_], annotation:Class[_ <: JAnnotation]):List[Field] =
    clazz.getDeclaredFields.filter { field =>
      field.isAnnotationPresent(annotation)
    } .toList

  def getModelsWithAnnotation(annotation:Class[_ <: JAnnotation]):List[Class[_]] =
    generatorModel.getModelsWithAnnotation(annotation)
}
