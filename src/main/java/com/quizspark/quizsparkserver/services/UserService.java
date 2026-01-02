package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.UserStatisticDTO;
import com.quizspark.quizsparkserver.dto.repositories.PracticeStatisticsDTO;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserStatisticDTO getUserStatistic() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserStatisticDTO userStatisticDTO = new UserStatisticDTO();
        long totalQuestionBanks = userRepository.getQuestionBankCount(user.getId());
        PracticeStatisticsDTO practiceStatistics = userRepository.getPracticeStatistics(user.getId());
        userStatisticDTO.setTotalQuestionBanks(totalQuestionBanks);
        userStatisticDTO.updatePracticeStatistics(practiceStatistics);
        return userStatisticDTO;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUsersByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(username);
        return user;
    }

    public User getUserByEmail(String email) {
        User user = userRepository.getUsersByEmail(email);
        if (user == null)
            throw new UsernameNotFoundException(email);
        return user;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }

    public void saveUser(User user) {
        if (userRepository.getUsersByUsername(user.getUsername()) != null)
            return;
        userRepository.save(user);
    }
}
