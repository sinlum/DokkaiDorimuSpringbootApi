package com.DokkaiDorimu.config;

import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitialDataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("Sin Lum Awng Hpauyu");
                admin.setEmail("sinlum.hpauyu@gmail.com");
                admin.setImgUrl("https://st.depositphotos.com/1036149/3347/i/450/depositphotos_33472707-stock-photo-giraffe.jpg");
                admin.setRole(User.Role.ADMIN);
                admin.setPassword(passwordEncoder.encode("senglung"));
                userRepository.save(admin);

                User creator = new User();
                creator.setUsername("Naw Naw");
                creator.setEmail("sinlum.hpauyujapan@gmail.com");
                creator.setImgUrl("https://st.depositphotos.com/1036149/3347/i/450/depositphotos_33472707-stock-photo-giraffe.jpg");
                creator.setRole(User.Role.CREATOR);
                creator.setPassword(passwordEncoder.encode("sinlumaung"));
                userRepository.save(creator);

                User user = new User();
                user.setUsername("Ko Sein");
                user.setEmail("sinlum.00@gmail.com");
                user.setImgUrl("https://st.depositphotos.com/1036149/3347/i/450/depositphotos_33472707-stock-photo-giraffe.jpg");
                user.setRole(User.Role.USER);
                user.setPassword(passwordEncoder.encode("sinlumaung"));
                userRepository.save(user);
            }
        };
    }
}