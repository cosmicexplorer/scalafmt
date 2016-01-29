package org.scalafmt

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

import org.scalatest.exceptions.TestFailedException

object DiffUtil extends ScalaFmtLogger {

  def assertNoDiff(a: String, b: String): Boolean = {
    val result = compareContents(a, b)
    if (result.isEmpty) true
    else throw new TestFailedException(
      s"""
         |${header("Obtained")}
         |${trailingSpace(a)}
         |
           |${header("Diff")}
         |${trailingSpace(result)}
         """.stripMargin, 1)

  }

  def trailingSpace(str: String): String = {
    str.replaceAll(" \n", "∙\n")
  }

  def compareContents(original: String, revised: String): String = {
    compareContents(original.split("\n"), revised.split("\n"))
  }

  def compareContents(original: Seq[String],
                      revised: Seq[String]): String = {
    import collection.JavaConverters._
    val diff = difflib.DiffUtils.diff(original.asJava, revised.asJava)
    if (diff.getDeltas.isEmpty) ""
    else difflib.DiffUtils.generateUnifiedDiff(
      "original", "revised", original.asJava, diff, 1).asScala.drop(3).mkString("\n")
  }

  def fileModificationTimeOrEpoch(file: File): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z")
    if (file.exists)
      format.format(new Date(file.lastModified()))
    else {
      format.setTimeZone(TimeZone.getTimeZone("UTC"))
      format.format(new Date(0L))
    }
  }
}
