package edu.plag.service;

import edu.plag.entity.User
import edu.plag.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getUserById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun getUserByUsername(username: String): User? = userRepository.findByUsername(username)

    @Transactional
    fun createUser(user: User): User = userRepository.save(user)

    @Transactional
    fun deleteUser(id: Long) = userRepository.deleteById(id)
}