package kutman.dev.Kforum.service.jwt;

import kutman.dev.Kforum.model.User;

public interface JwtToolsService {

    String generate(User user);

    boolean validate(String token);

    String extractEmail(String token);

    String extractRole(String token);

}
