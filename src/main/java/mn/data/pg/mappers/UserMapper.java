package mn.data.pg.mappers;

import mn.data.pg.domain.User;
import mn.data.pg.dtos.UserDto;

import javax.inject.Singleton;

@Singleton
public class UserMapper {

    public User toEntity(UserDto userDto) {
        User user = User.builder().username(userDto.getUsername()).password(userDto.getPassword()).build();
        return user;
    }

    public UserDto toDto(User user) {
        return UserDto.builder().username(user.getUsername()).password(user.getPassword()).role(user.getRole()).build();
    }
}
