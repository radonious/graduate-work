package edu.plag.mapper

import edu.plag.dto.UserRequest
import edu.plag.dto.UserResponse
import edu.plag.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface UserMapper {

    /**
     * Преобразовывает Response в Entity
     * @param [userResponse] что нужно преобразовать
     * @return [User] результат преобразования
     */
    @Mappings(
        Mapping(ignore = true, target = "password"),
        Mapping(ignore = true, target = "refreshToken"),
        Mapping(source = "role", target = "role")
    )
    fun toEntity(userResponse: UserResponse): User

    /**
     * Преобразовывает Request в Entity
     * @param [userRequest] что нужно преобразовать
     * @return [User] результат преобразования
     */
    @Mappings(
        Mapping(ignore = true, target = "id"),
        Mapping(ignore = true, target = "refreshToken"),
        Mapping(source = "role", target = "role")
    )
    fun toEntity(userRequest: UserRequest): User

    /**
     * Преобразовывает Entity в Response
     * @param [user] что нужно преобразовать
     * @return [UserResponse] результат преобразования
     */
    @Mapping(source = "role", target = "role")
    fun toDto(user: User): UserResponse
}