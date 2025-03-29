package edu.plag.service;

import edu.plag.entity.User
import edu.plag.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
data class UserService(
    private val userRepository: UserRepository,
    private val  passwordEncoder: PasswordEncoder,
    // private val  userMapper: UserMapper,
) {

    // TODO: найти мапперы для котлина и сделать мапперы

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getUserById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun getUserByUsername(username: String): User? = userRepository.findByUsername(username)

    @Transactional
    fun createUser(user: User): User = userRepository.save(user)

    @Transactional
    fun deleteUser(id: Long) = userRepository.deleteById(id)
}