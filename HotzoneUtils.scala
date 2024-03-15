package cse512
import scala.math._
object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String ): Boolean = {
    // YOU NEED TO CHANGE THIS PART
    // return true // YOU NEED TO CHANGE THIS PART
    if (Option(queryRectangle).getOrElse("").isEmpty || Option(pointString).getOrElse("").isEmpty)
      return false
    var rec_array = queryRectangle.split(",")
    var rec_x1 = rec_array(0).trim.toDouble
    var rec_y1 = rec_array(1).trim.toDouble
    var rec_x2 = rec_array(2).trim.toDouble
    var rec_y2 = rec_array(3).trim.toDouble

    var pt_array = pointString.split(",")
    var pt_x = pt_array(0).trim.toDouble
    var pt_y = pt_array(1).trim.toDouble

    if (pt_x >= min(rec_x1, rec_x2) && pt_x <= max(rec_x1, rec_x2) && pt_y >= min(rec_y1, rec_y2) && pt_y <= max(rec_y1, rec_y2))
      return true
    else
      return false
  }

  // YOU NEED TO CHANGE THIS PART

}
