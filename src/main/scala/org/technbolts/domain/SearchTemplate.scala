package org.technbolts.domain

object SearchTemplate {
  def apply = new SearchTemplate {}
  def apply(offset:Int,limit:Int) = {
    val template = new SearchTemplate {}
    template.offset = offset;
    template.limit  = limit;
    template
  }
}

trait SearchTemplate {
  var offset:Int = _
  var limit:Int = 20
}

