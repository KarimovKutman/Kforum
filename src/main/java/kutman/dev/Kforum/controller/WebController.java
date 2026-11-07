package kutman.dev.Kforum.controller;

import jakarta.validation.Valid;
import kutman.dev.Kforum.model.Category;
import kutman.dev.Kforum.model.Comment;
import kutman.dev.Kforum.model.DTO.CreateCategoryDto;
import kutman.dev.Kforum.model.DTO.UserDto;
import kutman.dev.Kforum.model.Topic;
import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.repo.CategoryRepository;
import kutman.dev.Kforum.repo.UserRepository;
import kutman.dev.Kforum.service.crud.CategoryService;
import kutman.dev.Kforum.service.crud.CommentService;
import kutman.dev.Kforum.service.crud.TopicService;
import kutman.dev.Kforum.service.crud.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

// тут работает thymeleaf шаблонизатор и он не умеет работать с json файлами я просто засовываю в шаблон файлы и таймлиф всё разбирает
@Controller
public class WebController {

    private final CategoryService categoryService;
    private final TopicService topicService;
    private final CommentService commentService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public WebController(
            CategoryService categoryService,
            TopicService topicService,
            CommentService commentService,
            UserService userService,
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.categoryService = categoryService;
        this.topicService = topicService;
        this.commentService = commentService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // это метод home он просто перекидывает на главную страницу через шаблонизаторы в главном  меню есть getAllCategories и топики благодаря методам сервисов
    // и ретурном мы говорим какой именно шаблон мы хотим показать home.html который находится в templates
    // когда я скачал тайлиф фреймворк спринг автоматически в своем dispatcheservlet находит фреймворк и там жестко говорится если метод вернул строку то автоматически превратить это в путь по формуле папка + строка + расширение а что бы спринг понял что это не путь нужно вставить в класс либо RestController либо ResponseBody за методом
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("topics", topicService.getTopicsSorted("newest", 0, 8));
        return "home";
    }

    // возвращает страницу с категориями
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("categoryStats", categoryService.getCategoryStats());
        return "categories";
    }

    // он смотрит на переменную Long id и по названию переменной в теле находит название переменной в параметрах и так кароче определяет. а сам айди мы находим через findById через repoCategory.findById(id)
    // Аннотация @PathVariable служит для того, чтобы вытащить переменную часть из URL-адреса (пути запроса). ну типа я ввел {id} 5 и так патхвалиабл работает вместе с getMapping и ищет айди 5 в бд  и так находит категорию
    // RedirectAttributes работает именно как временный буфер для передачи данных через пропасть редиректа. Чтобы сообщение (например, об успешном создании или ошибке) не потерялось по пути, используется механизм Flash-атрибутов (addFlashAttribute)
    @GetMapping("/categories/{id}")
    public String categoryDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).orElse(null);
        // если категория пуста он вернет ошибку иначе он вернет саму категорию
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Категория не найдена.");
            return "redirect:/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("topics", topicService.getTopicsByCategory(id));
        return "category-details";
    }
    // если запрос успешный он вернет путь. а если провальный то я ввожу специальную команду и когда спринг видит редирект то он перенаправляет меня куда то и я уведомление блять вывожу как то я хз. я понял. кароче редирект это просто название а не часть ссылки. это команда на которую стоит тригер у спринга. он должен перекинуть на страницу (времменную, после удачной попытки или ошикби)
    // модель это хранилище данных если ее использовать как временное хранилище  для сообщений типа ошибки или удачной попытки то данные просто стерутся так как создается совершенно чистая новая модель и старую никакими связямми не поддерживают
    @GetMapping("/topics/{id}")
    public String topicDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Topic existing = topicService.getTopicById(id);
            Topic topic = topicService.viewTopic(existing.getId());
            List<Comment> comments = commentService.getCommentsByTopic(id);
            model.addAttribute("topic", topic);
            model.addAttribute("comments", comments);
            return "topic-details";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Тема не найдена.");
            return "redirect:/";
        }
    }

    @GetMapping("/topics/new")
    public String newTopic(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "topic-form";
    }

    @PostMapping("/topics")
    public String createTopic(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long categoryId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String cleanTitle = title == null ? "" : title.trim();
        String cleanContent = content == null ? "" : content.trim();

        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заполни заголовок и текст темы.");
            return "redirect:/topics/new";
        }
        if (cleanTitle.length() > 200) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заголовок не должен быть длиннее 200 символов.");
            return "redirect:/topics/new";
        }

        try {
            User author = currentUser(authentication);
            Topic topic = topicService.createTopic(cleanTitle, cleanContent, author.getId(), categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Тема опубликована.");
            return "redirect:/topics/" + topic.getId();
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/topics/new";
        }
    }

    @PostMapping("/topics/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Комментарий не может быть пустым.");
            return "redirect:/topics/" + id;
        }

        try {
            User author = currentUser(authentication);
            commentService.addComment(content, id, author.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Комментарий добавлен.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/topics/" + id;
    }

    // реквайрд это означает что это что то обязательное тоесть пользователю нужно обязатель ввести этот запрос. если он не ввел и нажал энтер то по умолчанию вернется defaultValue = ""
    @GetMapping("/search")
    public String search(@RequestParam(required = false, defaultValue = "") String query, Model model) {
        Page<Topic> results = topicService.searchTopics(query, 0, 30);
        model.addAttribute("query", query);
        model.addAttribute("topics", results);
        return "search-results";
    }

    // В круглых скобках () этого метода пусто, потому что для показа страницы входа (/login)
    // серверу не нужно принимать никаких данных от пользователя. 🚪
    //
    //Когда человек просто открывает страницу авторизации, он ничего не передает в адресной
    // строке — ему нужно лишь отдать готовый HTML-шаблон с полями ввода
    // у логина нет шаблона так как у нео есть login.html  и гет это значит вернуть или показать этот шаблон ну кароче там чета с получить
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // это форма окна
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new UserDto());
        return "register";
    }

    // снаружи аннотация метода ModelAttribute работает как локальное автовызывание перед тем как вызвать любой метод а в параметрах как связывающее звено когда автоматически пишет гет сет и я так думаю что в параметрах аннотации я указываю ссылку на метод в котором лежит модель и я туда передаю данные которые типа гет и сетом заполнились из дто ну типа из дто беру данные и передаю в модель
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") UserDto registrationForm,
            BindingResult bindingResult,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        // Приведение email к нижнему регистру с помощью Locale.ROOT:
        // Без указания Locale метод toLowerCase() использует языковые настройки сервера.
        // Например, в турецком языке буква 'I' преобразуется в 'ı' (без точки), что ломает поиск email в базе.
        // Locale.ROOT задает универсальный международный стандарт, гарантируя,
        // что код будет работать одинаково на любом компьютере или сервере в любой точке мира.
        // Нормализация и проверка уникальности ключевых полей (email и username):
        // 1. Поля email и username выступают в роли уникальных идентификаторов для входа в систему.
        // 2. Метод .trim() убирает случайные пробелы по краям, а .toLowerCase(Locale.ROOT)
        //    приводит email к единому регистру(без больших букв), предотвращая создание дубликатов
        //    (например, чтобы Test@mail.com и test@mail.com считались одной записью).
        // 3. Остальные поля (например, пароль или имя) обрабатываются иначе:
        //    пароль чувствителен к регистру символов, а имя не требует проверки на уникальность в базе данных.
        String email = registrationForm.getEmail() == null ? "" : registrationForm.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = registrationForm.getUsername() == null ? "" : registrationForm.getUsername().trim();
        registrationForm.setEmail(email);
        registrationForm.setUsername(username);

        // тут я проверяю и накапливаю ошибки если они есть потом иду в главный иф hasErrors если там тру то я возврощаюсь в окно регистрации заного
        if (!confirmPassword.equals(registrationForm.getPassword())) {
            bindingResult.rejectValue("password", "password.mismatch", "Пароли не совпадают.");
        }
        if (!email.isEmpty() && userRepository.existsByEmailIgnoreCase(email)) {
            bindingResult.rejectValue("email", "email.exists", "Этот email уже зарегистрирован.");
        }
        if (!username.isEmpty() && userRepository.existsByUsername(username)) {
            bindingResult.rejectValue("username", "username.exists", "Это имя пользователя уже занято.");
        }

        /*
         * 3. Почему при ошибках возвращается строка "register", а не редирект (redirect:)?
         *    - Когда мы возвращаем обычную строку "register", это происходит в рамках ОДНОГО И ТОГО ЖЕ HTTP-запроса.
         *    - Объект Model и объект DTO со всеми введенными пользователем данными СОХРАНЯЮТСЯ.
         *    - Шаблон успевает прочитать эти данные и ошибки, поэтому пользователю не нужно заново
         *      заполнять форму — все его тексты остаются в инпутах.
         *    - Если бы мы сделали redirect, создал бы НОВЫЙ HTTP-запрос, старая модель стерлась бы,
         *      и поля формы очистились.
         */

        // 🛡️ Это специальный контейнер для результатов проверки (валидации). Когда перед UserDto стоит аннотация @Valid, Spring проверяет поля на соответствие правилам (например, корректность email). Если правила нарушены, Spring не падает с ошибкой, а записывает все замечания внутрь BindingResult. Проверка if (bindingResult.hasErrors()) позволяет поймать эти ошибки и не пустить пользователя дальше.
        // кароче там с валидом никак не связано. я вручную проверяю если пороли не совпадают то я возвращаюсь в регистер возможно это тоже временная хуйня как редирект но нет там же строка обычная возвращается так что это про модель
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(registrationForm);
            redirectAttributes.addFlashAttribute("successMessage", "Аккаунт создан. Теперь войди в систему.");
            return "redirect:/login";
        } catch (RuntimeException exception) {
            model.addAttribute("registrationError", "Не удалось создать аккаунт. Проверь email и имя пользователя.");
            return "register";
        }
    }

    // что за админ? что этот мтеод делает
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("categoryForm", new CreateCategoryDto());
        return "admin";
    }

    // что такое биндинг результ
    @PostMapping("/admin/categories")
    public String createCategory(
            @Valid @ModelAttribute("categoryForm") CreateCategoryDto categoryForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin";
        }

        // что такое флеш атрибут и куда я я их добовляю и ч то такое  "redirect:/admin"; в конце я выбрасываю
        try {
            categoryService.createCategory(categoryForm);
            redirectAttributes.addFlashAttribute("successMessage", "Категория создана.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Категория удалена.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin";
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Сначала войди в аккаунт.");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));
    }
}
