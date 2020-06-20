package mn.data.pg.repositories;

import java.util.List;
import java.util.UUID;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import mn.data.pg.domain.Message;
import mn.data.pg.domain.User;

@Repository
public interface MessageRepository extends PageableRepository<Message, UUID> {
    List<Message> findAllByUserRef(User user);
}

