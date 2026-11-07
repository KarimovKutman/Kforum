
package kutman.dev.Kforum.repo;

import kutman.dev.Kforum.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    // как работают эти 2 метода
    List<Topic> findByCategory_Id(Long categoryId);

    // что этот метод делает
    Page<Topic> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title,
            String content,
            Pageable pageable
    );
}
