package com.quizspark.quizsparkserver.services;

import com.quizspark.quizsparkserver.dto.classrooms.ClassroomCreateDTO;
import com.quizspark.quizsparkserver.dto.classrooms.ClassroomDTO;
import com.quizspark.quizsparkserver.dto.classrooms.ClassroomDetailsDTO;
import com.quizspark.quizsparkserver.models.Classroom;
import com.quizspark.quizsparkserver.models.ClassroomMember;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.repositories.ClassroomMemberRepository;
import com.quizspark.quizsparkserver.repositories.ClassroomRepository;
import com.quizspark.quizsparkserver.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;
    private final UserRepository userRepository;

    public ClassroomService(ClassroomRepository classroomRepository,
            ClassroomMemberRepository classroomMemberRepository,
            UserService userService,
            UserRepository userRepository) {
        this.classroomRepository = classroomRepository;
        this.classroomMemberRepository = classroomMemberRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public ClassroomDTO createClassroom(ClassroomCreateDTO createDTO) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User owner = userService.getUserById(principal.getId());

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

        return new ClassroomDTO(classroom);
    }

    public List<ClassroomDTO> getClassrooms() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return classroomMemberRepository.findByUserId(user.getId()).stream()
                .map(member -> new ClassroomDTO(member.getClassroom()))
                .collect(Collectors.toList());
    }

    public ClassroomDTO inviteUser(UUID classroomId, String username) {
        User inviterPrincipal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User inviter = userService.getUserById(inviterPrincipal.getId());

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

        return new ClassroomDTO(classroom);
    }

    public ClassroomDTO joinClassroom(String joinCode) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserById(principal.getId());

        Classroom classroom = classroomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Classroom not found with join code: " + joinCode));

        // Check if already a member
        boolean alreadyMember = classroom.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (alreadyMember) {
            throw new RuntimeException("User is already a member of this classroom");
        }

        ClassroomMember newMember = new ClassroomMember();
        newMember.setClassroom(classroom);
        newMember.setUser(user);
        newMember.setRole(ClassroomMember.ClassroomRole.STUDENT);
        classroomMemberRepository.save(newMember);

        return new ClassroomDTO(classroom);
    }

    public ClassroomDetailsDTO getClassroomById(UUID id) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserById(principal.getId());

        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        boolean isMember = classroom.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new RuntimeException("You are not a member of this classroom");
        }

        return new ClassroomDetailsDTO(classroom);
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
