import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

interface Writer {
    fun write(jsonStr : String, nameOfFile: String)
}

interface Reader {
    fun read(nameOfFile: String) : String?
}

/** Класс, записывающий в файлы данные */
class Serializer : Writer, Reader {
    init {
        // При инициализации создаю папку data, если она еще не создана.
        if (!Files.exists(Paths.get("./data"))) {
            Files.createDirectory(Paths.get("./data"))
        }
    }

    /** Записывает в файл nameOfFile строку jsonStr */
    override fun write(jsonStr: String, nameOfFile: String) {
        try {
            val path = "./data/$nameOfFile"
            File(path).bufferedWriter().use { out ->
                out.write(jsonStr)
            }
        } catch (ex : Exception) {
            println("Произошла ошибка при записи в файл $nameOfFile.")
        }
    }

    /** Читает из файла nameOfFile
     * @return все содержимое файла или null, если произошла ошибка  */
    override fun read(nameOfFile: String): String? {
        return try {
            val path = "./data/$nameOfFile"
            File(path).bufferedReader().use { out -> out.readText() }
        } catch (ex : Exception) {
            null
        }
    }
}