package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.ClassroomCreateDTO;
import com.quizspark.quizsparkserver.dto.ClassroomResponseDTO;
import com.quizspark.quizsparkserver.models.Classroom;
import com.quizspark.quizsparkserver.models.ClassroomMember;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.ClassroomMemberRepository;
import com.quizspark.quizsparkserver.repositories.ClassroomRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassroomService {
    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    private final UserRepository userRepository;

    public ClassroomService(ClassroomRepository classroomRepository,
            ClassroomMemberRepository classroomMemberRepository,
            UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.classroomMemberRepository = classroomMemberRepository;
        this.userRepository = userRepository;
    }

    public ClassroomResponseDTO createClassroom(ClassroomCreateDTO createDTO, User owner) {
        Classroom classroom = new Classroom();
        classroom.setName(createDTO.getName());
        classroom.setDescription(createDTO.getDescription());
        classroom.setJoinCode(generateUniqueJoinCode());

        classroom = classroomRepository.save(classroom);

        ClassroomMember member = new ClassroomMember();
        member.setClassroom(classroom);
        member.setUser(owner);
        member.setRole(ClassroomMember.ClassroomRole.OWNER);
        classroomMemberRepository.save(member);

        return new ClassroomResponseDTO(classroom);
    }

    public List<ClassroomResponseDTO> getClassroomsForUser(User user) {
        return user.getClassrooms().stream()
                .map(member -> new ClassroomResponseDTO(member.getClassroom()))
                .collect(Collectors.toList());
    }

    public ClassroomResponseDTO inviteUser(UUID classroomId, String username, User inviter) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        // Check if inviter is OWNER or TEACHER
        boolean canInvite = classroom.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(inviter.getId()) &&
                        (m.getRole() == ClassroomMember.ClassroomRole.OWNER
                                || m.getRole() == ClassroomMember.ClassroomRole.TEACHER));

        if (!canInvite) {
            throw new RuntimeException("Only owners or teachers can invite users");
        }

        User invitee = userRepository.getUsersByUsername(username);
        if (invitee == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Check if already a member
        boolean alreadyMember = classroom.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(invitee.getId()));

        if (alreadyMember) {
            throw new RuntimeException("User is already a member of this classroom");
        }

        ClassroomMember newMember = new ClassroomMember();
        newMember.setClassroom(classroom);
        newMember.setUser(invitee);
        newMember.setRole(ClassroomMember.ClassroomRole.STUDENT);
        classroomMemberRepository.save(newMember);

        return new ClassroomResponseDTO(classroom);
    }

    private String generateUniqueJoinCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            code = sb.toString();
        } while (classroomRepository.existsByJoinCode(code));
        return code;
    }
}
