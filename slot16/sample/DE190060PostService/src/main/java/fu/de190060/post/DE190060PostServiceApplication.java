package fu.de190060.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DE190060PostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DE190060PostServiceApplication.class, args);
    }
}
