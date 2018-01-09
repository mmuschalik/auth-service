package Model


case class Account(id: Int, name: String, activated: Boolean)
case class User(id: Int, accountId: Int, userName: String, email: String, password: String, properties: Map[String, String])
case class Token(str: String)
case class Credential(username: String, password: String)
case class Role(id: Int, name: String)
case class Permission(id: Int, permissionId: String)
case class Group(id: Int, parentGroupId: Int, name: String)
case class Session(user: User)

// Commands
case class RegisterAccount(name: String, userName: String, email: String, password: String)
case class AddUser(userName: String, email: String, password: String)
