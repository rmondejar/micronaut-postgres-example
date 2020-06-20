package mn.data.pg.services;

import java.util.Optional;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import mn.data.pg.domain.User;
import mn.data.pg.dtos.UserDto;
import mn.data.pg.mappers.UserMapper;
import mn.data.pg.repositories.UserRepository;

@Slf4j
@Singleton
public class UserService {

    private final UserRepository usersRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository usersRepository, UserMapper userMapper) {
        this.usersRepository = usersRepository;
        this.userMapper = userMapper;
    }

    public UserDto createUser(UserDto userDto) {
        User user = usersRepository.save(userMapper.toEntity(userDto));
        return userMapper.toDto(user);
    }

    public Optional<UserDto> findUser(String username) {
        return usersRepository.findByUsername(username).map(userMapper::toDto);
    }

    public Optional<UserDto> findByRefreshToken(String refreshToken) {
        return usersRepository.findByToken(refreshToken).map(userMapper::toDto);
    }

    public void saveRefreshToken(String username, String refreshToken) {
        usersRepository.findByUsername(username).ifPresent(
                user -> {
                    user.setToken(refreshToken);
                    usersRepository.update(user);
                }
        );
    }
}