
package kutman.dev.Kforum.service.crud;

import kutman.dev.Kforum.model.Comment;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    Comment addComment(String content, Long topicId, Long authorId);

    List<Comment> getCommentsByTopic(Long topicId);

    Page<Comment> getCommentsByTopic(Long topicId, int page, int size);

    Comment updateComment(Long commentId, String content, Long currentUserId);

    void deleteComment(Long commentId, Long currentUserId);
}
