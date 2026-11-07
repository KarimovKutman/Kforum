package kutman.dev.Kforum.service.crud;

import kutman.dev.Kforum.model.DTO.ChangePasswordDto;
import kutman.dev.Kforum.model.DTO.EmailAndPassword;
import kutman.dev.Kforum.model.DTO.UserDto;
import kutman.dev.Kforum.model.User;


public interface UserService {

    String register(UserDto user);
    String login(EmailAndPassword login);
    User findUserByUsername(String username);
    public void changePassword(ChangePasswordDto dto);
    void blockUser(String email);
    void unblockUser(String email);
}
