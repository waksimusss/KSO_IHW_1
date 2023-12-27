import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class User {
    // Сериализуем с помощью СПЕЦИАЛЬНОГО класса (он описан ниже)
    @Serializable(with = UsersDataSerializer::class)
    var login: String = ""

    // Сериализуем с помощью СПЕЦИАЛЬНОГО класса (он описан ниже)
    @Serializable(with = UsersDataSerializer::class)
    var password: String = ""

    // Объявляем конструктор через ключевое слово constructor с целью объявления уникальнных геттеров и сеттеров
    constructor(log: String, password: String) {
        login = log
        this.password = password
    }

    /** Метод, котоырй производит попытку входа в аккаунт
     * @return bool - удалась ли попытка входа */
    fun tryToEnter(loginEnter: String, passwordEnter: String): Boolean {
        return login == loginEnter && password == passwordEnter
    }
}

/** Объект-сериализатор для логина и пароля.
 * Нужен с целью шифрования логина и пароля */
internal object UsersDataSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserData", PrimitiveKind.STRING)

    /** Зашифровывает строку.
     * @return Зашифрованную с помощью протокола AES строку */
    override fun serialize(encoder: Encoder, value: String) {
        // Используя Encryptor, шифруем значение value
        val string = Encryptor.encryptThis(value)
        encoder.encodeString(string)
    }

    /** Расшифровывает строку.
     * @return Расшифрованную с помощью протокола AES строку */
    override fun deserialize(decoder: Decoder): String {
        val string = decoder.decodeString()
        return Encryptor.decrypt(string)
    }
}