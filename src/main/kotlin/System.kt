import kotlinx.serialization.json.Json
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.Duration.Companion.minutes
import kotlinx.serialization.encodeToString

/** Класс, управляющий поведением кинотеатра. */
class System(private var input: IInput) : ICinemable, IEdditable, IMarkable {
    // Различные списки и переменные, нужные для работы кинотеатра
    private var writer = Serializer()
    private var currentMovies = mutableListOf<Movie>()
    private var activeSessions = mutableListOf<Session>()
    private var tickets = mutableListOf<Ticket>()
    private var users = mutableListOf<User>()
    private var currentUser: User? = null

    // Доступно на чтение, но запрет на запись с внешних классов
    var isEntered = false
        private set

    init {
        // Читаем данные, оставшиеся с предыдущих запусков программы
        readDataFromFile()
        // Если у нас не введены параметры зала, то их нужно ввести
        if (pair.first == 0) {
            enterHallParameters()
        }
        // Помечаем для каждого из классов, с какого id им надо начинать создавать объекты
        if (currentMovies.isNotEmpty()) {
            filmId = currentMovies.maxOf { it.id }
        }
        if (activeSessions.isNotEmpty()) {
            sessionId = activeSessions.maxOf { it.id }
        }
        if (tickets.isNotEmpty()) {
            stId = tickets.maxOf { it.id }
        }
    }

    /** Печатает меню на экран
     * @return решение пользователя */
    fun printMenu(): Int {
        println(makeTextGreen("\nВыберите нужное действие: "))
        println("1. Добавить фильм")
        println("2. Добавить сеанс на существующий фильм")
        println("3. Оформить билет на сеанс")
        println("4. Вернуть билет на сеанс")
        println("5. Показать свободные места на сеансы")
        println("6. Редактировать данные о фильме")
        println("7. Редактировать данные о сеансе")
        println("8. Отметить посетителя на начало сеанса")
        println("9. Завершить работу программы")
        print(makeTextGreen("Ваш ввод: "))

        var choice: Int? = readln().toIntOrNull()
        while (choice == null || choice < 0 || choice > 9) {
            print("Вы ввели неверное число. Повторите ввод -> ")
            choice = readln().toIntOrNull()
        }

        return choice
    }

    /** Проверяет, есть ли такой пользователь в БД пользователей
     * @return объект пользователя, если данные корректны или null, если не корректны */
    fun isCorrectUser(login: String, password: String): User? {
        val user = users.find { it.tryToEnter(login, password) }
        if (user == null) {
            return null
        } else {
            isEntered = true
            currentUser = user
            return user
        }
    }

    /** Регистрирует нового пользоваля в системе по его логину и паролю */
    fun addNewUser(login: String, password: String) {
        users.add(User(login, password))
        serializeAll()
        println(makeTextGreen("Новый пользователь был добавлен. "))
    }

    /** Добавляет новый фильм в прокат кинотеатра */
    override fun addFilm() {

        currentMovies.add(input.readMovieData())
        serializeAll()
        println(makeTextGreen("Новый фильм был добавлен. "))
    }

    /** Добавляет новый сеанс в прокат кинотеатра */
    override fun addSession() {

        val session = input.readSessionData(currentMovies)
        if (session != null) {
            activeSessions.add(session)
            serializeAll()
            println(makeTextGreen("Новый сеанс был добавлен. "))
        }
    }

    /** Добавляет новый билет в список купленных билетов */
    override fun buyTicket() {

        val ticket = input.readInfoForTicket(activeSessions)
        if (ticket != null) {
            // Находим сеанс в списке сеансов
            val session = activeSessions.find { it.id == ticket.session }

            // Помечаем место занятым
            session?.markEngaged(ticket.place.first, ticket.place.second)

            // Добавляем в список билетов
            tickets.add(ticket)
            serializeAll()
            println(makeTextGreen("Покупка прошла успешно. ID вашего билета: ${ticket.id}"))
        }
    }

    /** Аннулирует билет и делает место в зале пустым */
    override fun returnTicket() {

        val ticket = input.readInfoForReturn(tickets)
        if (ticket != null) {
            // Находим сеанс в списке сеансов
            val session = activeSessions.find { it.id == ticket.session }

            val date = LocalDateTime.parse(session?.date, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            if (LocalDateTime.now() > date) {
                print("Возврат билета во время сеанса невозможен.")
                return
            }

            // Помечаем место пустым
            session?.cancelPlace(ticket.place.first, ticket.place.second)
            serializeAll()
            println(makeTextGreen("Возврат билета с ID = ${ticket.id} прошел успешно."))
        }
    }

    /** Показывает состояние зрительного зала для конкретного сеанса */
    override fun showCurrentSessions() {
        if (activeSessions.isEmpty()) {
            println("Сейчас не проходит ни одного сеанса. ")
            return
        }
        println("Введите id сеанса, который вас интересует или -1, если хотите вернуться обратно -> ")
        // Для удобства сортируем сеансы по дате их выполнения
        activeSessions.sortBy { it.date }

        val allIds: MutableList<Int> = mutableListOf()
        for (session in activeSessions) {
            allIds.add(session.id)
            println(session)
        }
        var id: Int? = readln().toIntOrNull()
        while (id == null || !allIds.contains(id)) {
            if (id == -1) {
                return
            }
            print("Вы ввели неверное число. Повторите ввод -> ")
            id = readln().toIntOrNull()
        }

        // Для сеанса с введенным ID показываем состояние зрительного зала
        activeSessions.find { it.id == id }?.showHall()
    }

    /** Ввод параметров зрительного зала */
    override fun enterHallParameters() {
        val params = input.readHallParameters()
        pair = params
        writer.write(Json.encodeToString(pair), "hallParams.log")
    }

    /** Метод, позволяющий изменить характеристики фильма */
    override fun editFilm() {
        if (currentMovies.isEmpty()) {
            println("Нет фильмов в прокате.")
        } else {
            // Считываем id фильма из консоли
            val film = input.readFilmId(currentMovies)
            if (film != null) {
                println("Что вы хотите поменять в фильме? Название (1), длительность (2) или возрастное ограничение (3)? ")
                println("При неверном вводе ничего не произойдет -> ")
                when (readln().toIntOrNull()) {
                    1 -> {
                        println("Введите новое название: ")
                        film.name = readln()
                    }

                    2 -> {
                        // Изменение продолжительности фильма
                        film.duration = input.readNonZeroLong().minutes
                    }

                    3 -> {
                        // Изменение возрастного ограничения фильма
                        film.ageLimits = input.readAgeLimit()
                    }

                    else -> {
                        println("Неверный ввод.")
                    }
                }
            }
            serializeAll()
        }
    }

    /** Метод, позволяющий изменить характеристики сеанса */
    override fun editSession() {
        // В сеансе можно поменять только дату
        if (activeSessions.isEmpty()) {
            println("Нет сеансов в прокате.")
        } else {
            // Вводим id интересущего нас сеанса
            val session = input.readSessionId(activeSessions)
            if (session != null) {
                println("Введите новое время сеанса в формате день-месяц-год часы:минуты -> ")
                var dateTime: LocalDateTime? = null
                while (dateTime == null) {
                    try {
                        // Парсим дату в определенном паттерне
                        dateTime = LocalDateTime.parse(readln(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))

                        // Смотрим чтобы новое время не было меньше текущего
                        if (dateTime < LocalDateTime.now()) {
                            throw DateTimeException("Нельзя забронировать сеанс в прошлом. Повторите ввод -> ")
                        }
                    } catch (ex: DateTimeParseException) {
                        print("Повторите ввод. Неверно введенная дата -> ")
                    } catch (ex: DateTimeException) {
                        print(ex.message)
                    }
                }
                session.date = dateTime.toString()
                serializeAll()
            }
        }
    }

    /** Помечает человека, как пришедщего на сеанс */
    override fun comeToSession() {
        if (activeSessions.isEmpty()) {
            println("Нет сеансов в прокате.")
        } else {
            println("Введите номер билета человека, который пришел, или -1 если хотите выйти -> ")
            val allIds = mutableListOf<Int>()
            for (ticket in tickets) {
                allIds.add(ticket.id)
            }

            var id: Int? = readln().toIntOrNull()
            while (id == null || !allIds.contains(id)) {
                if (id == -1) {
                    return
                }
                print("Вы ввели неверное число. Повторите ввод -> ")
                id = readln().toIntOrNull()
            }

            // Находим нужный нам билет по его id
            val ticket = tickets.find { it.id == id }
            if (ticket != null) {
                val session = activeSessions.find{it.id == ticket.session}
                // Помечаем человека как пришедшего
                session?.markCame(ticket.place.first, ticket.place.second)
                serializeAll()
                println(makeTextGreen("Посетитель с id  = ${ticket.id} был отмечен пришедшим на сеанс с id = ${ticket.session}"))
            }

        }
    }

    /** Считывает записанные в формате JSON данные о работе кинотеатра с файлов */
    private fun readDataFromFile() {
        try {
            pair = Json.decodeFromString(writer.read("hallParams.log")!!)
        } catch (_: Exception) {
        }
        try {
            currentMovies = Json.decodeFromString(writer.read(pathToFilms)!!)
        } catch (_: Exception) {
        }
        try {
            activeSessions = Json.decodeFromString(writer.read(pathToSessions)!!)
        } catch (_: Exception) {
        }
        try {
            tickets = Json.decodeFromString(writer.read(pathToTickets)!!)
        } catch (_: Exception) {
        }
        try {
            users = Json.decodeFromString(writer.read(pathToUsers)!!)
        } catch (_: Exception) {
        }
    }

    /** Записывает данные о работе кинотеатра в файлы */
    private fun serializeAll() {
        writer.write(Json.encodeToString(currentMovies), pathToFilms)
        writer.write(Json.encodeToString(activeSessions), pathToSessions)
        writer.write(Json.encodeToString(tickets), pathToTickets)
        writer.write(Json.encodeToString(users), pathToUsers)
    }
}