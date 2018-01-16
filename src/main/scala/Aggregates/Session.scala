package Aggregates

import ValueObjects.Token

class Session(private var _id: Int, val userId: Int, val token: Token, val expiry: Long) extends Entity {

    def id = _id

    private [Aggregates] def setId(newId: Int): Unit = _id = newId
}