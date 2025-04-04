import com.ktor.data.mapper.MessageMapper.toDomain
import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import com.ktor.domain.model.Message
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt

object UserMapper {

    // Mapea un UserRequestDTO a un User (hasheando la contraseña)
    fun UserRequestDTO.toDomain(): Pair<User, String> {
        val hashedPassword = BCrypt.hashpw(this.password, BCrypt.gensalt()) // 🔒 Hasheamos la contraseña
        val user = User(
            id = "",  // El ID lo asignará MongoDB
            username = this.username,
            email = this.email)

        return Pair(user, hashedPassword) // ✅ Retornamos el usuario y la contraseña hasheada
    }

    fun UserResponseDTO.toDomain(): User {
        return User(
            id = this.id,
            username = this.username,
            email = this.email,
        )
    }

    fun Document.toUserDomain(): User {
        return User(
            id = this["id"]?.toString() ?: "",
            username = this["username"] as? String ?: "",
            email = this["email"] as? String ?: ""
        )
    }

}
