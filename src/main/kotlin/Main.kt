fun main() {
    // Инициализируем работы систему
    val input = ConsoleInput()
    val cinema = System(input)

    // Предусматриваем вход администраторов в систему
    while (!cinema.isEntered) {
        println("Вы не авторизованы. " + makeTextGreen("Введите 1, если вы зарегистрированы в системе или 2, если хотите зарегистрироваться"))
        var choice = readln().toIntOrNull()
        while (choice == null || choice < 1 || choice > 2) {
            println("Неверный ввод. Повторите ввод. ")
            choice = readln().toIntOrNull()
        }
        when (choice) {
            1 -> {
                print("Введите логин -> ")
                val login = readln()
                print("Введите пароль -> ")
                val password = readln()

                val user = cinema.isCorrectUser(login, password)
                // Если попытка входа успешна, то мы авторизировались
                if (user != null) {
                    println("${user.login}, вы успешно авторизованы!")
                }
            }

            2 -> {
                println("Введите логин для входа в аккаунт -> ")
                val login = readln()
                println("Введите пароль для входа в аккаунт -> ")
                val password = readln()
                cinema.addNewUser(login, password)
            }
        }
    }

    // В зависимости от ввода пользователя, система производит определенные действия
    var choice: Int
    do {
        choice = cinema.printMenu()
        when (choice) {
            1 -> {
                cinema.addFilm()
                println()
            }

            2 -> {
                cinema.addSession()
                println()
            }

            3 -> {
                cinema.buyTicket()
                println()
            }

            4 -> {
                cinema.returnTicket()
                println()
            }

            5 -> {
                cinema.showCurrentSessions()
                println()
            }

            6 -> {
                cinema.editFilm()
                println()
            }

            7 -> {
                cinema.editSession()
                println()
            }

            8 -> {
                cinema.comeToSession()
                println()
            }
            9 -> {
                println("Завершение работы программы")
                break
            }
        }
    } while (true)
    // команда "9" является выходом из программы
}