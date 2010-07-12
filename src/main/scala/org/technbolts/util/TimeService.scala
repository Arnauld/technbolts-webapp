package org.technbolts.util
import org.springframework.stereotype.Service

trait TimeService {
  import _root_.java.util.{Date => JDate}
  import _root_.java.text.SimpleDateFormat

  var pattern = "HH:mm:ss"

  def getTime = new JDate
  def getFormattedTime = new SimpleDateFormat(pattern).format(getTime)
}

@Service("timeService")
class TimeServiceImpl extends TimeService
