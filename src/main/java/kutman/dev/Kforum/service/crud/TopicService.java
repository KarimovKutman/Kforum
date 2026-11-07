
package kutman.dev.Kforum.service.crud;

import kutman.dev.Kforum.model.Topic;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TopicService {

    Topic createTopic(String title, String content,
                      Long authorId, Long categoryId);

    List<Topic> getTopicsByCategory(Long categoryId);

    Topic getTopicById(Long id);

    Topic updateTopic(Long topicId, String title, String content,
                      Long currentUserId);

    void deleteTopic(Long topicId, Long currentUserId);

    Page<Topic> searchTopics(String keyword, int page, int size);

    Page<Topic> getTopicsSorted(String sortBy, int page, int size);

    Topic viewTopic(Long id);
}
