package Model

case class Account(id: Int, name: String, activated: Boolean)
case class User(id: Int, accountId: Int, userName: String, email: String, password: String, properties: Map[String, String])
case class Token(str: String)
case class Role(id: Int, name: String)
case class Group(id: Int, parentGroupId: Int, name: String)
case class Session(token: Token, user: User, expiry: Long)

case class Resource(id: String)

sealed trait ResourceAccessControl
class ResourceIdList(verb: String, ids: Seq[String]) extends ResourceAccessControl

case class Permission(id: Int, resource: Resource, resourceAccessControl: Seq[ResourceAccessControl])

// Commands
case class RegisterAccount(name: String, userName: String, email: String, password: String)
case class AddUser(userName: String, email: String, password: String)
