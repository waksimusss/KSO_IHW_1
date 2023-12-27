import kotlinx.serialization.Serializable

/** Зрительный зал, в котором можно занять места */
@Serializable
class Hall(private val numOfRows : Int, private var numOfSeats : Int) {
    // Имитация зрительного зала массивом мест
    private var places = Array(numOfRows) { Array(numOfSeats){Place()} }

    init {
        for (row in places) {
            var temp = 1
            val rowS = (row.indices.last() + 1).toString().length
            for (i in row.indices) {
                row[i].setNewId("%0${rowS}d".format(temp))
                ++temp
            }
        }
    }

    /** @return количество свободных мест в данном зрительном зале */
    fun countFreePlaces() : Int {
        var free = 0
        for (row in places) {
            for (place in row) {
                if (place.toString()[0] != 'X' && place.toString()[0] != '=') {
                    ++free
                }
            }
        }
        return free
    }

    /** @return bool - свободно ли место или нет */
    fun isFreePlace(row : Int, place : Int) : Boolean {
        return try {
            !places[row - 1][place - 1].isPlaced
        } catch (_: Exception) {
            false
        }
    }

    /** Бронирует место согласно его номеру */
    fun bookPlace(row : Int, place : Int) {
        try {
            places[row - 1][place - 1].isPlaced = true
        } catch (_: Exception) { }
    }

    /** Помечает, что зритель пришел на свое место */
    fun markCame(row : Int, place : Int) {
        try {
            places[row - 1][place - 1].isCame = true
        } catch (_: Exception) { }
    }

    /** Переводит место в статус свободного (обычно, при отмене билета( */
    fun cancelPlace(row : Int, place : Int) {
        try {
            places[row - 1][place - 1].isPlaced = false
        } catch (_: Exception) { }
    }

    override fun toString(): String {
        var str = ""
        var rowId = 1
        val rows = (places.size + 1).toString().length
        for (row in places) {
            str += "${"%0${rows}d".format(rowId)} ->  "
            for (place in row) {
                str += "$place "
            }
            str += "\n"
            ++rowId
        }
        str += makeTextGreen("X - забронированные места; + - места, на которые уже пришли люди. \n")
        return str
    }
}

/** Вспомогательный класс "Место" */
@Serializable
class Place {
    private var id : String = ""
    var isPlaced : Boolean = false
    var isCame = false

    /** Помечает новый id у места */
    fun setNewId(placeId : String) {
        id = placeId
    }

    override fun toString(): String {
        return if (isCame) {
            "+".repeat(id.length)
        } else if (isPlaced) {
            "X".repeat(id.length)
        } else {
            id
        }
    }
}