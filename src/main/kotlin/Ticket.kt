import kotlinx.serialization.Serializable

@Serializable
class Ticket(var session: Int, var place : Pair<Int, Int>) {
    var id : Int = 0

    init {
        id = ++stId
    }
}