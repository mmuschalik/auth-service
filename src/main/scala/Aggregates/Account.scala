package Aggregates

class Account(private var _id: Int, var name: String, private var activated: Boolean, val activationKey: String) extends Entity {

  def id = _id

  private [Aggregates] def setId(newId: Int): Unit = _id = newId

  def activate(): Unit = {
    activated = true
  }

  def isActive: Boolean = activated
}

object Account {
  def apply(name: String): Account = new Account(0, name, false, java.util.UUID.randomUUID().toString)
}