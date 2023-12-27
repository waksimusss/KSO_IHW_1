import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Позволяет вводить исходные данные из консоли
 */
class ConsoleInput : IInput {
    /** Считывает с консоли возрастное ограничение */
    override fun readAgeLimit(): AgeLimits {
        println("Введите возрастное ограничение: одну из строк 0+, 6+, 12+, 16+ или 18+.")
        print("При неправильном вводе возрастное ограничение станет равным NotIdentified -> ")
        return when (readln()) {
            "0+" -> AgeLimits.ZeroPlus
            "6+" -> AgeLimits.SixPlus
            "12+" -> AgeLimits.TwelvePlus
            "16+" -> AgeLimits.SixteenPlus
            "18+" -> AgeLimits.EighteenPlus
            else -> AgeLimits.NotIdentified
        }
    }

    /** Считывает с консоли ненулевое число типа Long */
    override fun readNonZeroLong(): Long {
        var dur: Long? = readln().toLongOrNull()
        while (dur == null || dur < 0) {
            print("Вы ввели неверное число. Повторите ввод -> ")
            dur = readln().toLongOrNull()
        }
        return dur
    }

    /** Считывает с консоли информацию о фильме
     * @return объект типа Film
     * */
    override fun readMovieData(): Movie {
        print("Введите имя фильма -> ")
        val tempName = readln()

        print("Введите продолжительность фильма: число в минутах ->")
        val duration: Duration = readNonZeroLong().minutes

        val tempAge: AgeLimits = readAgeLimit()

        return Movie(tempName, duration, tempAge)
    }

    /** Считывает с консоли информацию о сеансе
     * @return объект типа Session или null, в случае возникновения ошибки */
    override fun readSessionData(listMovies: MutableList<Movie>): Session? {
        if (listMovies.isEmpty()) {
            println("Сейчас в прокате нет никаких фильмов :( ")
            return null
        }
        println("Доступные для просмотра фильмы: ")
        // Формируем список со всеми ID всех фильмов
        val allIds: MutableList<Int> = mutableListOf()
        for (film in listMovies) {
            allIds.add(film.id)
            println(film)
        }

        println("Введите id фильма, сеанс которого вы создаете.")
        var id: Int? = readln().toIntOrNull()
        while (id == null || !allIds.contains(id)) {
            print("Вы ввели неверное число. Повторите ввод -> ")
            id = readln().toIntOrNull()
        }

        // Находим фильм из списка фильмов по введенному id.
        val movie: Movie = listMovies.find { it.id == id }!!

        // Считываем с консоли дату и время сеанса
        println("Введите дату и время, когда будет показан фильм в формате " + makeTextGreen("день-месяц-год часы:минуты"))
        var dateTime: LocalDateTime? = null
        while (dateTime == null) {
            try {
                dateTime = LocalDateTime.parse(readln(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                if (dateTime < LocalDateTime.now()) {
                    throw DateTimeException("Нельзя создать сеанс в прошлом. Повторите ввод -> ")
                }
            } catch (ex: DateTimeParseException) {
                print("Повторите ввод. Неверно введенная дата -> ")
                dateTime = null
            } catch (ex: DateTimeException) {
                print(ex.message)
                dateTime = null
            }
        }

        return Session(movie, dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), Hall(pair.first, pair.second))
    }

    /** Считывает с консоли информацию о зрительном зале
     * @return Пару из двух чисел: длина и ширина зрительного зала */
    override fun readHallParameters(): Pair<Int, Int> {
        print("Введите количество рядов: ")
        var numOfRows: Int? = readln().toIntOrNull()
        while (numOfRows == null || numOfRows < 0) {
            print("Вы ввели неверное число. Повторите ввод -> ")
            numOfRows = readln().toIntOrNull()
        }

        print("Введите количество мест в ряду: ")
        var numOfSeats: Int? = readln().toIntOrNull()
        while (numOfSeats == null || numOfSeats < 0) {
            print("Вы ввели неверное число. Повторите ввод -> ")
            numOfSeats = readln().toIntOrNull()
        }
        return Pair(numOfRows, numOfSeats)
    }

    /** Считывает информацию о билете с консоли
     * @return объект типа Ticket или null, если сеансов сейчас нет */
    override fun readInfoForTicket(list: MutableList<Session>): Ticket? {
        // Отображаем только те сеансы, где есть хотя бы одно свободное посадочное место
        val listSessions: MutableList<Session> = mutableListOf()
        for (session in list) {
            if (session.hallSession.countFreePlaces() != 0 && !LocalDateTime.parse(session.date, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")).isBefore(LocalDateTime.now())) {
                listSessions.add(session)
            }
        }

        if (listSessions.isEmpty()) {
            println("Нет доступных сеансов.")
            return null
        }

        println("Выберите сеанс, на который хотите купить билет или -1, если хотите вернуться обратно. ")
        val allIds: MutableList<Int> = mutableListOf()
        for (session in listSessions) {
            allIds.add(session.id)
            println(session)
        }
        // Считываем id сеанса
        var id: Int? = readln().toIntOrNull()
        while (id == null || !allIds.contains(id)) {
            if (id == -1) {
                // Можно ввести -1 и вернуться обратно в меню
                return null
            }
            print("Вы ввели неверное число. Повторите ввод -> ")
            id = readln().toIntOrNull()
        }
        val session = listSessions.find { it.id == id }

        println("Свободные места: ")
        session?.showHall()

        println("Введите номер ряда и номер места через пробел (нумерация с 1): ")
        try {
            var lst = readln().split(" ")
            var rowNum = lst[0].toIntOrNull()
            var placeNum = lst[1].toIntOrNull()
            // Считываем ряд и место через пробел.
            while (rowNum == null || placeNum == null || rowNum < 1 || rowNum > pair.first || placeNum < 1 || placeNum > pair.second || !session?.hallSession?.isFreePlace(rowNum, placeNum)!!) {
                // Проверяем, что пользователь не может сесть на уже занятое место)
                if (rowNum != null && placeNum != null && rowNum >= 1 && rowNum <= pair.first && placeNum >= 1 && placeNum <= pair.second && !session?.hallSession?.isFreePlace(rowNum, placeNum)!!) {
                    print("Это место уже занято. Выберите другое -> ")
                } else {
                    print("Неверный ввод чисел. Повторите ввод -> ")
                }
                lst = readln().split(" ")
                rowNum = lst[0].toIntOrNull()
                placeNum = lst[1].toIntOrNull()
            }
            return Ticket(session.id, Pair(rowNum, placeNum))
        } catch (_ : Exception) {
            println("Неверный ввод! ")
            return null
        }
    }

    /** Считывает с консоли информацию о билете, который необходимо вернуть
     * @return объект типа Ticket или null в случае ошибки */
    override fun readInfoForReturn(listTicket: MutableList<Ticket>): Ticket? {
        if (listTicket.isEmpty()) {
            println("Нет билетов в системе.")
            return null
        } else {
            println("Введите id вашего билета или -1, чтобы вернуться обратно: ")
            // Формируем список из всех id
            val allIds = mutableListOf<Int>()
            for (ticket in listTicket) {
                allIds.add(ticket.id)
            }

            var id: Int? = readln().toIntOrNull()
            while (id == null || !allIds.contains(id)) {
                if (id == -1) {
                    return null
                }
                print("Вы ввели неверное число. Повторите ввод -> ")
                id = readln().toIntOrNull()
            }

            return listTicket.find { it.id == id }
        }
    }

    /** Отдельно считывает id фильма и проверяет, что такой фильм существует
     * @return объект типа Film или null в случае ошибки */
    override fun readFilmId(listMovies: MutableList<Movie>): Movie? {
        println("Доступные фильмы: ")
        // Формируем список из всех id всех фильмов
        val allIds = mutableListOf<Int>()
        for (movie in listMovies) {
            println(movie)
            allIds.add(movie.id)
        }

        println("Введите id фильма, который вы хотите изменить или -1, если хотите вернуться обратно.")
        var id: Int? = readln().toIntOrNull()
        while (id == null || !allIds.contains(id)) {
            if (id == -1) {
                return null
            }
            print("Вы ввели неверное число. Повторите ввод -> ")
            id = readln().toIntOrNull()
        }

        // Возвращаем film, соответствующий этому id
        return listMovies.find { it.id == id }
    }

    /** Отдельно считывает id сеанса и проверяет, что такой сеанс существует
     * @return объект типа Session или null в случае ошибки */
    override fun readSessionId(listSessions: MutableList<Session>): Session? {
        println("Доступные сеансы: ")
        val allIds = mutableListOf<Int>()
        for (film in listSessions) {
            println(film)
            allIds.add(film.id)
        }
        listSessions.sortBy { it.date }
        println("Введите id сеанса, который вы хотите изменить или -1, если хотите вернуться обратно.")
        var id: Int? = readln().toIntOrNull()
        while (id == null || !allIds.contains(id)) {
            if (id == -1) {
                return null
            }
            print("Вы ввели неверное число. Повторите ввод -> ")
            id = readln().toIntOrNull()
        }
        return listSessions.find { it.id == id }
    }
}