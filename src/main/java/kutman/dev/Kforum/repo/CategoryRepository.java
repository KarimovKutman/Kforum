package kutman.dev.Kforum.repo;

import kutman.dev.Kforum.model.Category;
import kutman.dev.Kforum.model.DTO.CategoryStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    // этот кастомый запрос вовзвращает список статистики поста в виде дто. тоесть в жпкл мы засовывает айди нейм комент коунт и топик каунт
    @Query("""
    SELECT new kutman.dev.Kforum.model.DTO.CategoryStatsDto(
        c.id,
        c.name,
        COUNT(DISTINCT t.id),
        COUNT(DISTINCT cm.id)
    )
    FROM Category c
    LEFT JOIN Topic t ON t.category = c
    LEFT JOIN Comment cm ON cm.topic = t
    GROUP BY c.id, c.name
    ORDER BY c.name
    """)
    List<CategoryStatsDto> getCategoryStats();
}
