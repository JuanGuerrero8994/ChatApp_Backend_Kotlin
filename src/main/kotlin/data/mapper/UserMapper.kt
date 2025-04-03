import com.ktor.data.model.user.UserRequestDTO
import com.ktor.data.model.user.UserResponseDTO
import com.ktor.domain.model.User
import org.mindrot.jbcrypt.BCrypt

object UserMapper {

    // Mapea un UserRequestDTO a un User (hasheando la contraseña)
    fun UserRequestDTO.toDomain(): Pair<User, String> {
        val hashedPassword = BCrypt.hashpw(this.password, BCrypt.gensalt()) // 🔒 Hasheamos la contraseña
        val user = User(
            id = "",  // El ID lo asignará MongoDB
            username = this.username,
            email = this.email
        )
        return Pair(user, hashedPassword) // ✅ Retornamos el usuario y la contraseña hasheada
    }

    // Mapea un User a un UserResponseDTO (sin exponer la contraseña)
    fun User.toResponse(): UserResponseDTO {
        return UserResponseDTO(
            id = this.id,
            username = this.username,
            email = this.email
        )
    }


}
