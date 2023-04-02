package ua.dmjdev.TelegramGPTBot.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ua.dmjdev.TelegramGPTBot.models.User;

@Repository
public interface UserRepository extends MongoRepository<User, Long> {
    User findUserById(long id);
}
