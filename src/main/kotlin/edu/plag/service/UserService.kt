package edu.plag.service

import edu.plag.dto.UserRequest
import edu.plag.dto.UserResponse
import edu.plag.entity.User
import edu.plag.enums.UserRole
import edu.plag.mapper.UserMapper
import edu.plag.repository.UserRepository
import jakarta.persistence.EntityExistsException
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper,
) {

    /**
     * Запрос всех существующих пользователей
     * @return список [UserResponse]
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun findAllUsers(): List<UserResponse> {
        return userRepository.findAll().stream()
            .map { userMapper.toDto(it) }
            .toList()
    }

    /**
     * Поиск пользователя по ID
     * @param [id] ID пользователя
     * @return [UserResponse] ответ
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun findUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow { EntityNotFoundException("User not found with id: $id") }
        return userMapper.toDto(user)
    }

    /**
     * Поиск пользователя по имени
     * @param [username] имя пользователя
     * @return [UserResponse] ответ
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun findUserByUsername(username: String): UserResponse {
        val user: User = userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("User not found with username: $username") }
        return userMapper.toDto(user)
    }

    /** Создание нового пользователя
     * @param [UserRequest] запрос
     * @return [UserResponse] ответ
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createUser(userRequest: UserRequest): UserResponse {
        val user: User = userMapper.toEntity(userRequest.copy(role = UserRole.USER))
        if (userRepository.existsByUsername(user.username)) {
            throw EntityExistsException("Username '${user.username}' already exists")
        }
        val passwordCoded = passwordEncoder.encode(userRequest.password)
        val savedUser = userRepository.save(user.copy(password = passwordCoded))
        return userMapper.toDto(savedUser)
    }

    /**
     * Обновление данных о пользователе
     * @param [id] ID пользователя
     * @param [userRequest] запрос с новыми данными
     * @return [UserResponse] ответ
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun updateUser(id: Long, userRequest: UserRequest): UserResponse {
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("User not found with id: $id")
        }
        val updatedUser: User = userMapper.toEntity(userRequest).copy(id = id)
        val savedUser = userRepository.save(updatedUser)
        return userMapper.toDto(savedUser)
    }

    /**
     * Удаления пользователя по ID
     * @param id ID пользователя
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("User not found with id: $id")
        }
        userRepository.deleteById(id)
    }


}