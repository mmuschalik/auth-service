package ValueObjects

case class Session(userId: Int, accountId: Int, userName: String, token: Token, expiry: Long) {

}
