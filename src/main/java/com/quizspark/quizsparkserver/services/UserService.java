package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.UserStatisticDTO;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserStatisticDTO getUserStatistic() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.getUserStatistic(user.getId());
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUsersByUsername(username);
        if (user == null) throw new UsernameNotFoundException(username);
        return user;
    }

    public User getUserByEmail(String email) {
        User user = userRepository.getUsersByEmail(email);
        if (user == null) throw new UsernameNotFoundException(email);
        return user;
    }

    public void saveUser(User user) {
        if(userRepository.getUsersByUsername(user.getUsername()) != null) return;
        userRepository.save(user);
    }
}
