package utils

case class Page[+A](items: Seq[A], page: Int, size: Int, total: Long) {
  def offset = (page - 1) * size + 1
}