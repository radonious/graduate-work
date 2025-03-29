package edu.plag.service

import edu.plag.entity.User
import edu.plag.repository.UserRepository
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.userdetails.UserDetailsService as UserDetailsServiceInterface


@Service
class UserDetailsService(
    val userRepository: UserRepository,
) : UserDetailsServiceInterface {

    @Transactional(isolation = Isolation.READ_COMMITTED)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            AuthorityUtils.createAuthorityList("ROLE_" + user.role.name)
        )
    }
}