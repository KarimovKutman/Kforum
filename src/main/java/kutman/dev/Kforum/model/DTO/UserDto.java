package kutman.dev.Kforum.model.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserDto {

    @Size(min = 4, max = 16)
    @NotBlank
    private String username;

    @Email(message = "Неверный email")
    @NotBlank(message = "Email обязателен")
    private String email;


    @NotBlank
    @Size(min = 16, max = 64)
    // не надо разбирать как именно работает этот regexp нужно просто изучить механизм работы
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$", message = "Пароль должен содержать хотя бы 1 большую букву и цифру")
    private String password;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Имя не должно содержать цифры или символы")
    private String name;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Фамилия не должна содержать цифры или символы")
    private String surname;


    public UserDto(String username, String email, String password, String name, String surname) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public UserDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }



}



