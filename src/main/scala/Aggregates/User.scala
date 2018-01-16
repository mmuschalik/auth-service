package Aggregates

class User(private var _id: Int, val accountId: Int, private var _userName: String, private var _email: String, private var _password: String) extends Entity {

  def id: Int = _id
  def userName: String = _userName
  def email: String = _email
  def password: String = _password

  private[Aggregates] def setId(newId: Int): Unit = _id = newId

}