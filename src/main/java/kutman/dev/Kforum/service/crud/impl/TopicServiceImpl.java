
package kutman.dev.Kforum.service.crud.impl;

import kutman.dev.Kforum.model.Topic;
import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.model.Category;
import kutman.dev.Kforum.model.DTO.Role;
import kutman.dev.Kforum.repo.TopicRepository;
import kutman.dev.Kforum.repo.UserRepository;
import kutman.dev.Kforum.repo.CategoryRepository;
import kutman.dev.Kforum.service.crud.TopicService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TopicServiceImpl(
            TopicRepository topicRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public Topic createTopic(
            String title,
            String content,
            Long authorId,
            Long categoryId
    ) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Автор не найден"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Категория не найдена"));

        Topic topic = new Topic();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setAuthor(author);
        topic.setCategory(category);

        return topicRepository.save(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> getTopicsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Категория не найдена");
        }

        return topicRepository.findByCategory_Id(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Topic getTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тема не найдена"));
    }

    @Override
    @Transactional
    public Topic updateTopic(
            Long topicId,
            String title,
            String content,
            Long currentUserId
    ) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тема не найдена"));

        checkPermission(topic, currentUserId);

        topic.setTitle(title);
        topic.setContent(content);

        return topicRepository.save(topic);
    }

    @Override
    @Transactional
    public void deleteTopic(Long topicId, Long currentUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тема не найдена"));

        checkPermission(topic, currentUserId);

        topicRepository.delete(topic);
    }

    private void checkPermission(Topic topic, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Пользователь не найден"));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isAuthor =
                topic.getAuthor().getId().equals(currentUserId);

        if (!isAdmin && !isAuthor) {
            throw new SecurityException(
                    "Нет прав на редактирование или удаление этой темы");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Topic> searchTopics(
            String keyword,
            int page,
            int size
    ) {
        validatePagination(page, size);

        String query = keyword == null ? "" : keyword.trim();
        // сортируем на уменьшение и сортируем по дате создания тоесть сначала идут новейшие а потом старейшие
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return topicRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                        query, query, pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Topic> getTopicsSorted(
            String sortBy,
            int page,
            int size
    ) {
        validatePagination(page, size);
        Sort sort;

        if ("popular".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "views");
        } else if ("newest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else {
            throw new IllegalArgumentException(
                    "Сортировка должна быть newest или popular");
        }

        return topicRepository.findAll(
                PageRequest.of(page, size, sort)
        );
    }

    @Override
    @Transactional
    public Topic viewTopic(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Тема не найдена"));

        long currentViews = topic.getViews() == null
                ? 0L
                : topic.getViews();

        topic.setViews(currentViews + 1);

        return topicRepository.save(topic);
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
