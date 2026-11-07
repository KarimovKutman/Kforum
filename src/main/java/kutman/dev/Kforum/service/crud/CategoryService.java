package kutman.dev.Kforum.service.crud;

import kutman.dev.Kforum.model.Category;
import kutman.dev.Kforum.model.DTO.CategoryStatsDto;
import kutman.dev.Kforum.model.DTO.CreateCategoryDto;

import java.util.List;

public interface CategoryService {

    boolean existsByName(String name);

    Category createCategory(CreateCategoryDto dto);

    List<Category> getAllCategories();


    List<CategoryStatsDto> getCategoryStats();

    Category updateCategory(Long id, CreateCategoryDto dto);

    void deleteCategory(Long id);
}