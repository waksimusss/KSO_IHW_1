import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
class Movie(var name: String, var duration: Duration, var ageLimits: AgeLimits) {
    var id : Int = 0
    init {
        id = ++filmId
    }

    override fun toString(): String {
        return "ID = $id. Название = $name. Продолжительность = $duration. Возрастное ограничение = $ageLimits"
    }

}