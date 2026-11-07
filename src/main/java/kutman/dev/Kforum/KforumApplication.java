package kutman.dev.Kforum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// почему я в этом классе нечего не пишу? а что будет если написать что нибудь слушай при запуске я постоянно вижу что какой то пороль генерируется за меня. меня это уже бесит я хочу избавится от текста 456546-45645645-46564--3-4- generated password for developement only я хочу создавать свои ключи. как мне это сделать
@SpringBootApplication
public class KforumApplication {

	public static void main(String[] args) {
		SpringApplication.run(KforumApplication.class, args);
	}
}