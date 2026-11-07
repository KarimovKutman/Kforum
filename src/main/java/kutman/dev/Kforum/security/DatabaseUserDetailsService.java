package kutman.dev.Kforum.security;

import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.model.DTO.Role;
import kutman.dev.Kforum.repo.UserRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// @Service сообщает Spring, что этот класс является сервисным компонентом.
// Spring автоматически создаёт объект этого класса (Bean) и управляет им.
// Благодаря этому другие компоненты могут получать DatabaseUserDetailsService
// через конструктор, не создавая объект вручную.
//
// DatabaseUserDetailsService — наш собственный класс.
// Он реализует UserDetailsService — интерфейс Spring Security.
//
// Этот интерфейс устанавливает контракт: класс должен уметь загружать
// пользователя по имени пользователя (в нашем случае вместо имени используется email).
// кароче мы используем этот сервис как проверку доступа
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Конструкторная инъекция зависимостей.
    // Spring сам передаёт сюда готовый о бъект UserRepository.
    // Через него мы будем обращаться к базе данных.
    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Главный метод интерфейса UserDetailsService.
    //
    // Spring Security вызывает его во время аутентификации,
    // когда необходимо найти пользователя по введённому email.
    //
    // Наша задача — найти пользователя в БД и вернуть его данные
    // в формате UserDetails, который понимает Spring Security.
    //
    // Если пользователя не существует, выбрасываем UsernameNotFoundException.
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Ищем пользователя в базе данных по email.
        //
        // trim() удаляет пробелы в начале и конце email.
        // findByEmailIgnoreCase() ищет без учёта регистра:
        // например, Test@gmail.com и test@gmail.com считаются одинаковыми.
        //
        // orElseThrow() выбрасывает исключение, если пользователь не найден.
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Неверный email или пароль")
                );

        // Проверяем, заблокирован ли аккаунт.
        // Если заблокирован, выбрасываем DisabledException.
        // Это сообщает Spring Security, что аккаунт отключён
        // и его нельзя использовать для входа.
        if (user.isBlocked()) {
            throw new DisabledException("Аккаунт заблокирован");
        }

        // Получаем роль пользователя из базы данных.
        //
        // Здесь используется тернарный оператор:
        // условие ? значение_если_true : значение_если_false
        //
        // Если роль пользователя равна null (не установлена),
        // назначаем ему роль USER.
        // Иначе используем его настоящую роль из базы данных.
        Role role = user.getRole() == null
                ? Role.USER
                : user.getRole();

        // Создаём объект UserDetails — специальный объект Spring Security.
        //
        // ВАЖНО: это НЕ наша сущность model.User из базы данных.
        // Это класс Spring Security, в котором хранятся данные,
        // необходимые для проверки пользователя при входе.
        //
        // withUsername() — задаём email в качестве имени пользователя.
        //
        // password() — передаём пароль из базы данных.
        // Обычно здесь должен находиться уже закодированный пароль
        // (например, BCrypt-хеш), а не обычный текст.
        //
        // authorities() — назначаем права пользователя.
        // role.name() возвращает название enum-роли, например USER или ADMIN.
        // Префикс ROLE_ — стандартное соглашение Spring Security
        // для обозначения ролей: ROLE_USER, ROLE_ADMIN и т.д.
        //
        // build() завершает создание объекта UserDetails
        // и возвращает его Spring Security.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + role.name())
                .build();
    }
}