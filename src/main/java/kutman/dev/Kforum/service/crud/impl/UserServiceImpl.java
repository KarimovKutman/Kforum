package kutman.dev.Kforum.service.crud.impl;

import kutman.dev.Kforum.model.DTO.ChangePasswordDto;
import kutman.dev.Kforum.model.DTO.EmailAndPassword;
import kutman.dev.Kforum.model.DTO.UserDto;
import kutman.dev.Kforum.model.DTO.Role;
import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.repo.UserRepository;
import kutman.dev.Kforum.service.crud.UserService;
import kutman.dev.Kforum.service.exceptionService.InvalidPasswordException;
import kutman.dev.Kforum.service.exceptionService.UserAlreadyExistsException;
import kutman.dev.Kforum.service.exceptionService.UserNotFoundException;
import kutman.dev.Kforum.service.jwt.JwtToolsService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;


@Service
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder bcrypt;
    private final JwtToolsService jwtToolsService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder bcrypt, JwtToolsService jwtService) {
        this.userRepository = userRepository;
        this.bcrypt = bcrypt;
        this.jwtToolsService = jwtService;
    }

    @Transactional
    @Override
    public String register(UserDto user) {
        try{
            String hashedPassword = bcrypt.encode(user.getPassword());
            User newUser = new User(
                    user.getUsername().trim(),
                    user.getEmail().trim().toLowerCase(Locale.ROOT),
                    hashedPassword,
                    user.getName().trim(),
                    user.getSurname().trim()
            );
            newUser.setRole(Role.USER);
            newUser.setBlocked(false);

            // флуш это отправка и после этого сразу флуш (тоесть синхронизировать с бд) кароче, когда мы
            // отправляем что то через  сейв оно может засунуть ну кароче эта хуйня которую ты
            // обязательно забудишь так как мне лень объяснять. просто запомни флуш это
            // "сосредоточься на этом объекте и отправь в бд и потом жди"
            // после того как данные придут ошибка либо сообщениео о успехе код вернется и
            // продолжит работать. а там хуйня какая то с пресист типа пресист не обязательно
            // может отправить в бд. он там чета подготовить должен и код возвращается в мой
            // класс так как возможно там строчки закончились и там уже следующий уровень
            // углубляется. ну блять мне так чат гпт сказал мне потом стало лень дальше его
            // допрашивать я уже заебался. заебал он меня. кароче я понял мне объяснили типа
            // то что у каждого сессии свой персистанс контекст следовательно у каждого потока
            // своя "корзина" временная память нужна не для складывания а потом одним махом всё
            // в бд, нет, она  нужна что бы можно было делать dirty checking и вот появляется
            // проблема. транзакшонал то может закончится. и в конце отпарвит флуш. это обычный
            // последдовательный код нету никаких сигналов. это просто обертка. получается в
            // конце он делает flush после того как блять. заебал, кароче мне лень объяснять
            // эту поеботень так же мне лень залезать в анус транзакшонала или какойто другой
            // хуйни что бы понять по настоящему. квен отправил текст прочитай
            //его и ты достаточно поймешь интуитивно типа.
            //
            //1. Ты вызываешь: userRepository.save(newUser)
            //   ↓
            //2. Spring Data JPA вызывает: entityManager.persist(newUser)
            //   ↓
            //3. Hibernate создает EntityInsertAction и кладет в ActionQueue (в RAM)
            //   ↓
            //4. Твой метод заканчивается
            //   ↓
            //5. Spring-прокси вызывает: entityManager.flush()
            //   ↓
            //6. Hibernate проходит по ActionQueue, для каждого EntityInsertAction:
            //   - Генерирует SQL-строку: "INSERT INTO users (email, password) VALUES (?, ?)"
            //   - Создает PreparedStatement: connection.prepareStatement(sql)
            //   - Подставляет значения: statement.setString(1, "test@mail.com")
            //   ↓
            //7. 🔥 ВЫЗОВ: statement.executeUpdate()
            //   ↓
            //8. JDBC-драйвер отправляет SQL по сети в базу данных (через TCP-сокет)
            //   ↓
            //9. База данных выполняет запрос и возвращает результат
            userRepository.saveAndFlush(newUser);

            // не еби себе голову. поймешь когда реально будет надо, где хранится этот ебучий жвт ключ
            return jwtToolsService.generate(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("не удалось зарегистрироваться");
        }
    }

    @Override
    public String login(EmailAndPassword login) {

        User user = userRepository.findByEmailIgnoreCase(login.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Неверный логин или пароль"));

        if (user.isBlocked()) {
            throw new IllegalStateException("Аккаунт заблокирован");
        }

        if (!bcrypt.matches(login.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Неверный логин или пароль");
        }

        return jwtToolsService.generate(user);
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("данный пользователь не найден"));
    }

    @Override
    public void changePassword(ChangePasswordDto dto) {

        User user = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("Неверный логин или пароль"));

        if (!bcrypt.matches(dto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Неверный логин или пароль");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        user.setPassword(bcrypt.encode(dto.getNewPassword()));

        userRepository.save(user);
    }

    @Override
    public void blockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден"));

        user.setBlocked(true);
        userRepository.save(user);
    }

    @Override
    public void unblockUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден"));

        user.setBlocked(false);
        userRepository.save(user);
    }
}
