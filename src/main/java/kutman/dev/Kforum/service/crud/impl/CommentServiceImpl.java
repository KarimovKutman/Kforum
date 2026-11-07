
package kutman.dev.Kforum.service.crud.impl;

import kutman.dev.Kforum.model.Comment;
import kutman.dev.Kforum.model.Topic;
import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.model.DTO.Role;
import kutman.dev.Kforum.repo.CommentRepository;
import kutman.dev.Kforum.repo.TopicRepository;
import kutman.dev.Kforum.repo.UserRepository;
import kutman.dev.Kforum.service.crud.CommentService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            TopicRepository topicRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Comment addComment(
            String content,
            Long topicId,
            Long authorId
    ) {
        String cleanedContent = validateContent(content);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тема не найдена"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Автор не найден"));

        Comment comment = new Comment();
        comment.setContent(cleanedContent);
        comment.setTopic(topic);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByTopic(Long topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new IllegalArgumentException("Тема не найдена");
        }

        return commentRepository
                .findByTopic_IdOrderByCreatedAtAsc(topicId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByTopic(
            Long topicId,
            int page,
            int size
    ) {
        if (!topicRepository.existsById(topicId)) {
            throw new IllegalArgumentException("Тема не найдена");
        }

        validatePagination(page, size);
        // PageRequest.of(page, size) — это способ сказать Spring Data JPA: «Верни мне конкретную страницу комментариев и укажи, сколько комментариев должно быть на одной странице».
        return commentRepository.findByTopic_IdOrderByCreatedAtAsc(
                topicId,
                PageRequest.of(page, size)
        );
    }

    @Override
    @Transactional
    public Comment updateComment(
            Long commentId,
            String content,
            Long currentUserId
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Комментарий не найден"));

        checkPermission(comment, currentUserId);
        comment.setContent(validateContent(content));

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Комментарий не найден"));

        checkPermission(comment, currentUserId);
        commentRepository.delete(comment);
    }

    private String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Текст комментария не может быть пустым");
        }

        return content.trim();
    }

    private void checkPermission(Comment comment, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Пользователь не найден"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isAuthor =
                comment.getAuthor().getId().equals(currentUserId);

        if (!isAdmin && !isAuthor) {
            throw new SecurityException(
                    "Нет прав на изменение или удаление комментария");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Номер страницы не может быть отрицательным");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Размер страницы должен быть от 1 до 100");
        }
    }
}
