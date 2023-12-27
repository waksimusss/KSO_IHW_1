import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

/** Класс-"Сеанс", отвечающий за характеристики конкретного сеанса в кинотеатре */
@Serializable
class Session(private var movie : Movie, var date: String, var hallSession : Hall) {
    var id : Int = 0
    init {
        id = ++sessionId
    }

    /** Помечает место занятым */
    fun markEngaged(row: Int, place: Int) {
        hallSession.bookPlace(row, place)
    }

    /** Помечает, что человек пришел на свое место */
    fun markCame(row: Int, place: Int) {
        hallSession.markCame(row, place)
    }

    /** Помечает место свободным */
    fun cancelPlace(row: Int, place: Int) {
        hallSession.cancelPlace(row, place)
    }

    /** Выводит на экран текущее состояние зрительного зала */
    fun showHall() {
        println(hallSession)
    }

    override fun toString(): String {
        return "ID = $id. Название фильма = ${movie.name}. Дата сеанса = ${date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}. Свободные места: ${hallSession.countFreePlaces()}"
    }

}