package org.technbolts.mda

/**
 * very simple template engine
 */
trait Template {

  var NL = "\r\n"
  var INDENT = "  ";

  def subVars(text: String, vars: Map[String, String]):String = {
    def key2Pattern(key: String) = ("(?i)\\$\\{"+key+"\\}").r
    var result = text
    for (key <- vars.keys)
      result = key2Pattern(key).replaceAllIn(result, vars(key))
    result
  }
}