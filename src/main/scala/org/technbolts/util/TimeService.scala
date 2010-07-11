package org.technbolts.util
import org.springframework.stereotype.Service
@Service("timeService")
class TimeService {
  def getTime = new _root_.java.util.Date
  def getFormattedTime = new _root_.java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(getTime)
}
