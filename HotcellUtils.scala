package cse512

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

import scala.collection.mutable.ListBuffer

object HotcellUtils {
  val coordinateStep = 0.01

  def CalculateCoordinate(inputString: String, coordinateOffset: Int): Int =
  {
    // Configuration variable:
    // Coordinate step is the size of each cell on x and y
    var result = 0
    coordinateOffset match
    {
      case 0 => result = Math.floor((inputString.split(",")(0).replace("(","").toDouble/coordinateStep)).toInt
      case 1 => result = Math.floor(inputString.split(",")(1).replace(")","").toDouble/coordinateStep).toInt
      // We only consider the data from 2009 to 2012 inclusively, 4 years in total. Week 0 Day 0 is 2009-01-01
      case 2 => {
        val timestamp = HotcellUtils.timestampParser(inputString)
        result = HotcellUtils.dayOfMonth(timestamp) // Assume every month has 31 days
      }
    }
    return result
  }

  def timestampParser (timestampString: String): Timestamp =
  {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val parsedDate = dateFormat.parse(timestampString)
    val timeStamp = new Timestamp(parsedDate.getTime)
    return timeStamp
  }

  def dayOfYear (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_YEAR)
  }

  def dayOfMonth (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_MONTH)
  }

  def neighbourCount (minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, inputX: Int, inputY: Int, inputZ: Int): Int =
  {
    var neighbour_ct = 0

    if (inputX == minX || inputX == maxX) {
      neighbour_ct += 1
    }

    if (inputY == minY || inputY == maxY) {
      neighbour_ct += 1
    }

    if (inputZ == minZ || inputZ == maxZ) {
      neighbour_ct += 1
    }

    if (neighbour_ct == 1) {
      return 17
    }
    else if (neighbour_ct == 2)
    {
      return 11
    }
    else if (neighbour_ct == 3)
    {
      return 7
    }
    else
    {
      return 26
    }
  }
  def square (n: Int): Double =
  {
    return (n * n).toDouble
  }
  def get_GScore (x: Int, y: Int, z: Int, mean: Double, std: Double, countN: Int, sumN: Int, numCells: Int): Double =
  {
    val n = sumN.toDouble - (mean * countN.toDouble)
    val d = std * math.sqrt((((numCells.toDouble * countN.toDouble) - (countN.toDouble * countN.toDouble)) / (numCells.toDouble - 1.0).toDouble).toDouble).toDouble
    return (n / d).toDouble
  }
  // YOU NEED TO CHANGE THIS PART
}
