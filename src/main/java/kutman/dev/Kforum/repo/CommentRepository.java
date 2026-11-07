
package kutman.dev.Kforum.repo;

import kutman.dev.Kforum.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {


    // он ищет по топику потому что в самом классе есть поле Topic нужно найти к какому топику он относится для начала
    List<Comment> findByTopic_IdOrderByCreatedAtAsc(Long topicId);

    // спринг возвращает полный список коментарием сортированый по дате с уменьшением
    Page<Comment> findByTopic_IdOrderByCreatedAtAsc(
            Long topicId,
            Pageable pageable
    );
}
