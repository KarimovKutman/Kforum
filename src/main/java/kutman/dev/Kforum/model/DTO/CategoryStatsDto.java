package kutman.dev.Kforum.model.DTO;

public class CategoryStatsDto {
    private Long id;
    private String name;
    private Long topicCount;
    private Long commentCount;

    public CategoryStatsDto(Long id , String name, Long topicCount, Long commentCount) {
        this.name = name;
        this.topicCount = topicCount;
        this.commentCount = commentCount;
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Long topicCount) {
        this.topicCount = topicCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
}
