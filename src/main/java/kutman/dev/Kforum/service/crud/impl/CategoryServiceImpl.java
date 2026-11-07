package kutman.dev.Kforum.service.crud.impl;

import kutman.dev.Kforum.model.Category;
import kutman.dev.Kforum.model.DTO.CategoryStatsDto;
import kutman.dev.Kforum.model.DTO.CreateCategoryDto;
import kutman.dev.Kforum.repo.CategoryRepository;
import kutman.dev.Kforum.service.crud.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * nameTaken
 * true
 *
 * !equalsIgnoreCase()
 * false
 *
 *
 * Итог: true && false = false. Ошибки нет.
 *
 * Ситуация B: меняешь название на Python, и оно свободно
 * nameTaken
 * false
 *
 * !equalsIgnoreCase()
 * true
 *
 *
 * Итог: false && true = false. Ошибки нет.
 *
 * Ситуация C: меняешь название на Spring, которое занято другой категорией
 * nameTaken
 * true
 *
 * !equalsIgnoreCase()
 * true
 *
 *
 * Итог: true && true = true. Выбрасывается ошибка.
 */

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name.trim());
    }

    // @Transactional нужен там, где несколько операций с базой данных должны выполниться как единое целое.
    @Override
    @Transactional
    public Category createCategory(CreateCategoryDto dto) {
        String cleanedName = dto.getName().trim();

        if (cleanedName.isEmpty()) throw new IllegalArgumentException(
                "Название категории не может быть пустым"
        );

        if (existsByName(cleanedName)) throw new IllegalArgumentException(
                "Категория с таким названием уже существует"
        );

        Category category = new Category();
        category.setName(cleanedName);

        // если дескрипшон нул то вернуть нулл либо отрезать лишние пробелы и вернуть чистый дескрипшон
        // мы берем с дто дескрипшон проверяем а потом засовываем этот дескрипшон в категорий объект а потом сохраняем в бд
        String description = dto.getDescription();
        category.setDescription(
                description == null ? null : description.trim()
        );

        return categoryRepository.save(category);
    }

    //вообщем рид онли он отключает функции редтактирования а так как редакт пропал то он и проверки лишает его. получается это оптимизация мы можем включать рид онли когда мы читаем и копируем данные из бд ну кароче без изменения
    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatsDto> getCategoryStats() {
        return categoryRepository.getCategoryStats();
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CreateCategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Категория не найдена"
                        ));
        // я могу вызвать трим потому что я возврощаю объект Стринг а у стринга есть трим и вот так вот получается цепочка
        // и так дто внутри себя хранит нейм и дескрипшон мы берем имя и это имя нам нужно для того что бы мы посмотрели есть ли ещё одна такая категория с таким именем если есть то выбрасываем ошибку и ещё если категория старая которая  мы достали из бд не совподает с именем из дто то мы выбрасываем ошибку
         String cleanedName = dto.getName().trim();

        if (cleanedName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Название категории не может быть пустым"
            );
        }

        boolean nameTaken = categoryRepository
                .existsByNameIgnoreCase(cleanedName);

        //Если такое название уже занято другой категорией И название отличается от текущего названия этой категории — выбросить ошибку.
        if (nameTaken && !category.getName().equalsIgnoreCase(cleanedName)) {
            throw new IllegalArgumentException(
                    "Категория с таким названием уже существует"
            );
        }

        category.setName(cleanedName);

        String description = dto.getDescription();
        category.setDescription(
                description == null ? null : description.trim()
        );

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Категория не найдена"
                        ));

        categoryRepository.delete(category);
    }
}