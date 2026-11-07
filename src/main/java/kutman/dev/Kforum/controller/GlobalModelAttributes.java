package kutman.dev.Kforum.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/*
 * @ControllerAdvice — это глобальный перехватчик (или помощник) для контроллеров.
 * Параметр `annotations = Controller.class` говорит Spring:
 * "Применяй этот класс ТОЛЬКО к тем контроллерам, которые помечены обычной аннотацией @Controller
 * (которые возвращают HTML-страницы через Thymeleaf)".
 * К @RestController (которые отдают чистый JSON для API) этот класс лезть не будет.
 */
// эта хуйня каоче в озу забирается потом таймлиф через свою магию забирает эти параметры к себе и вставляет в хтмл поля. там посмотри ты там найдешь тела типа ${isAuthenticated} ну вот. а потом юзеру отправляется
@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

    /*
     * модел атрибут это автоматическое запускание метода перед остальными методами которые должны были запустится
     * модел атрибут это локальная хуйня ее можно поместить на любой контроллер и он будет запускатся всегда первым получается можно просто засунуть этот метод в веб контроллер
     * получается контроллер адвайс привелкает всех а потом запускает моделатрибут
     * Откуда берется Authentication authentication?
     * Spring Security автоматически передает сюда объект текущей сессии пользователя,
     * если он залогинен, либо объект гостя, если запрос идет от незалогиненного посетителя.
     * Пользователь заходит на сайт.
     * Java открывает твой .html файл как обычный текст.
     * Thymeleaf сканирует этот текст, находит свои метки (вроде th:text="..."),
     * стирает их и вставляет туда реальные данные (например, имя из базы данных).
     * Получается новый кусок текста — чистый стандартный HTML без всяких следов Thymeleaf.
     * Сервер отправляет этот готовый текст в браузер.
     */
    @ModelAttribute
    public void addAuthenticationState(Model model, Authentication authentication) {

        /*
         * Что такое AnonymousAuthenticationToken?
         * Когда пользователь НЕ залогинен (просто гость зашел на сайт), Spring Security
         * все равно создает для него объект аутентификации, но особого типа — AnonymousAuthenticationToken.
         * <p>
         * Проверка ниже означает:
         * 1. authentication != null — объект вообще существует?
         * 2. authentication.isAuthenticated() — он прошел аутентификацию?
         * 3. !(authentication instanceof AnonymousAuthenticationToken) — ИНОГОРОДНИЙ ГОСТЬ ЛИ ОН?
         * Если это аноним, то выражение дает false. Реально залогиненным пользователь считается
         * только тогда, когда он вошел под своим аккаунтом (не аноним).
         */
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        /*
         * Как работает проверка на админа?
         * 1. authenticated — проверяем, вообще залогинен ли пользователь (если нет, то и роли проверять нет смысла).
         * 2. authentication.getAuthorities() — достаем список всех прав и ролей пользователя (например, ["ROLE_USER", "ROLE_ADMIN"]).
         * 3. .stream().map(GrantedAuthority::getAuthority) — превращаем коллекцию объектов прав в поток строк-названий.
         * 4. .anyMatch("ROLE_ADMIN"::equals) — ищем среди этих строк хотя бы одно совпадение со строкой "ROLE_ADMIN".
         * Если совпадение найдено, переменная admin становится true.
         * кароче там 3 4 аналог for i ищет в авторити строчки с надписем roleAdmin и там проверяет
         */
        boolean admin = authenticated && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        /*
         * model.addAttribute(...) — это метод сохранения данных в коробочку (Model),
         * чтобы потом в HTML-файлах (в Thymeleaf) можно было легко использовать эти флаги.
         * Например, в HTML написать: <div th:if="${isAuthenticated}">Привет!</div>
         * или скрыть/показать панель администратора через th:if="${isAdmin}".
         */
        model.addAttribute("isAuthenticated", authenticated);
        model.addAttribute("isAdmin", admin);

        // Если пользователь реально залогинен, достаем его email (или логин) через authentication.getName()
        // и тоже прокидываем в модель, чтобы вывести на экран (например, "Вы вошли как: user@mail.com").
        if (authenticated) {
            model.addAttribute("currentUserEmail", authentication.getName());
        }
    }
}