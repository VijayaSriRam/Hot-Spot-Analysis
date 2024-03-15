package cse512

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

object HotcellAnalysis {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)

def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame =
{

  var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter",";").option("header","false").load(pointPath);
  pickupInfo.createOrReplaceTempView("nyctaxitrips")
  pickupInfo.show()

  spark.udf.register("CalculateX",(pickupPoint: String)=>((
    HotcellUtils.CalculateCoordinate(pickupPoint, 0)
    )))
  spark.udf.register("CalculateY",(pickupPoint: String)=>((
    HotcellUtils.CalculateCoordinate(pickupPoint, 1)
    )))
  spark.udf.register("CalculateZ",(pickupTime: String)=>((
    HotcellUtils.CalculateCoordinate(pickupTime, 2)
    )))
  pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
  var newCoordinateName = Seq("x", "y", "z")
  pickupInfo = pickupInfo.toDF(newCoordinateName:_*)
  pickupInfo.createOrReplaceTempView("pickupInfo")
  pickupInfo.show()


  val minX = -74.50/HotcellUtils.coordinateStep
  val maxX = -73.70/HotcellUtils.coordinateStep
  val minY = 40.50/HotcellUtils.coordinateStep
  val maxY = 40.90/HotcellUtils.coordinateStep
  val minZ = 1
  val maxZ = 31
  val numCells = (maxX - minX + 1)*(maxY - minY + 1)*(maxZ - minZ + 1)

  spark.udf.register("square", (input: Int) => ((HotcellUtils.square(input))))
  spark.udf.register("countNeighbours", (minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, inputX: Int, inputY: Int, inputZ: Int) => ((HotcellUtils.neighbourCount(minX, minY, minZ, maxX, maxY, maxZ, inputX, inputY, inputZ))))
  spark.udf.register("GScore", (x: Int, y: Int, z: Int, mean:Double, std: Double, countN: Int, sumN: Int, numCells: Int) => ((HotcellUtils.get_GScore(x, y, z, mean, std, countN, sumN, numCells))))

  val q1: String = "select x,y,z from pickupInfo where (x between "+minX+" and "+maxX+") and (y between "+minY+" and "+maxY+") and (z between "+minZ+" and "+maxZ+") order by z,y,x"
  val valid_cells = spark.sql(q1)
  valid_cells.createOrReplaceTempView("Df0")

  val q2: String = "select x, y, z, count(*) as pointVal from Df0 group by z, y, x order by z, y, x"
  val pt_cnt = spark.sql(q2)
  pt_cnt.createOrReplaceTempView("Df1")

  val q3: String = "select count(*) as countVal, sum(pointVal) as sumVal, sum(square(pointVal)) as squaredSum from Df1"
  val pt_sum = spark.sql(q3)
  pt_sum.createOrReplaceTempView("sumofPoints")

  val mean = (pt_sum.first().getLong(1).toDouble / numCells.toDouble).toDouble
  val std = math.sqrt((pt_sum.first().getDouble(2).toDouble / numCells.toDouble) - (mean.toDouble * mean.toDouble)).toDouble

  val q4: String = "select countNeighbours(" + minX + "," + minY + "," + minZ + "," + maxX + "," + maxY + "," + maxZ + "," + " a1.x, a1.y, a1.z) as nCount, a1.x as x, a1.y as y, a1.z as z, sum(a2.pointVal) as sumTotal from Df1 as a1, Df1 as a2 where (a2.x = a1.x+1 or a2.x = a1.x or a2.x = a1.x-1) and (a2.y = a1.y+1 or a2.y = a1.y or a2.y = a1.y-1) and (a2.z = a1.z+1 or a2.z = a1.z or a2.z = a1.z-1) group by a1.z, a1.y, a1.x order by a1.z, a1.y, a1.x"
  val neighbours = spark.sql(q4)
  neighbours.createOrReplaceTempView("Df2");

  val q5: String = "select GScore(x, y, z, " + mean + ", " + std + ", nCount, sumTotal," +numCells+ ") as gtstat, x, y, z from Df2 order by gtstat desc"
  val g_score = spark.sql(q5)
  g_score.createOrReplaceTempView("Df3")

  val q6: String = "select x, y, z from Df3"
  val res = spark.sql(q6)
  res.createOrReplaceTempView("Df4")
  return res
  // return pickupInfo // YOU NEED TO CHANGE THIS PART
}
}
