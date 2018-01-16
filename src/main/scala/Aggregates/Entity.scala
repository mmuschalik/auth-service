package Aggregates

trait Entity {
  def id: Int
  private [Aggregates] def setId(newId: Int): Unit
}
