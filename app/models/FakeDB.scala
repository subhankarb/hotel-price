package models

import utils.Page
import scala.io.Source
import scala.collection.mutable

object FakeDB {
  private val hotelMap = mutable.Map(Source
    .fromInputStream(FakeDB.getClass.getResourceAsStream("/hoteldb.csv"))
    .getLines().drop(1).map { line: String =>
      val Array(city, hotelId, room, price) = line.split(",").map(_.trim)
      Hotel(city.toLowerCase, hotelId.toInt, room.toLowerCase, price.toInt)
    }.zipWithIndex.map { a => (a._2.toLong, a._1) }.toSeq: _*)

  val hotels = FakeTable(hotelMap, hotelMap.size.toLong)
  val apiKeys = FakeTable[ApiKey]()

  case class FakeTable[A](var table: mutable.Map[Long, A], var incr: Long) {

    def nextId: Long = {
      if (!table.contains(incr))
        incr
      else {
        incr += 1
        nextId
      }
    }
    def get(id: Long): Option[A] = table.get(id)
    def size: Int = table.size
    def find(p: A => Boolean): Option[A] = table.values.find(p)
    def values: List[A] = table.values.toList
    def isEmpty: Boolean = size == 0
    def insert(a: Long => A): (Long, A) = {
      val id = nextId
      val tuple = id -> a(id)
      table += tuple
      incr += 1
      tuple
    }
    def delete(id: Long): Unit = table -= id
    def deleteAll(): Unit = table.clear()
    def filter(p: A => Boolean): List[A] = values.filter(p)

    def page(page: Int, size: Int)(filterFunc: A => Boolean)(sortFunc: ((A, A) => Boolean)): Page[A] = {
      val items = filter(filterFunc)
      val sorted = items.sortWith(sortFunc)
      Page(
        items = sorted.slice((page - 1) * size, (page - 1) * size + size),
        page = page,
        size = if (sorted.size < size) sorted.size else size,
        total = sorted.size
      )
    }
  }

  object FakeTable {
    def apply[A](elements: (Long, A)*): FakeTable[A] = apply(mutable.Map(elements: _*), elements.size + 1)
  }
}
