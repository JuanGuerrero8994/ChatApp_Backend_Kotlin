import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import org.bson.Document


fun UserRequestDTO.toDomain() = User(
    email = email,
    password = password,
    username = username
)

fun User.toUserResponseDTO(): UserResponseDTO = UserResponseDTO(
    id = this.id ?: "",
    username = this.username ?: "",
    email = this.email ?: ""
)

fun Document.toUser(): User = User(
    id = this.getObjectId("_id").toString(),
    username = this.getString("username"),
    email = this.getString("email"),
    password = this.getString("passwordHash")
)


