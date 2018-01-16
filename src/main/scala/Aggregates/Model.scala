package Aggregates

// aggregates

class RoleAgg(id: Int, name: String)
class GroupAgg(id: Int, parentGroupId: Int, name: String)
class SessionAgg(id: Int, userId: Int, token: String, expiry: Long)

class UserPermissionAgg(id: Int, permissions: Seq[Permission])
class GroupPermissionAgg(id: Int, permissions: Seq[Permission])
class RolePermissionAgg(id: Int, permissions: Seq[Permission])

// value objects
trait Permission
case class PermissionByResource(resourceName: String, allowedVerbs: Seq[String]) extends Permission
case class PermissionByResourceIds(resourceName: String, allowedVerbs: Seq[String], ids: Seq[String]) extends Permission


// Commands
case class RegisterAccount(name: String, userName: String, email: String, password: String)
case class AddUser(userName: String, email: String, password: String)
