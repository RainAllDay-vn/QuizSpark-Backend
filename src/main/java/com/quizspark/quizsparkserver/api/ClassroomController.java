package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.ClassroomCreateDTO;
import com.quizspark.quizsparkserver.dto.ClassroomResponseDTO;
import com.quizspark.quizsparkserver.models.User;
import com.quizspark.quizsparkserver.services.ClassroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classrooms")
public class ClassroomController {
    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    public ResponseEntity<ClassroomResponseDTO> createClassroom(
            @RequestBody ClassroomCreateDTO createDTO,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.createClassroom(createDTO, user));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomResponseDTO>> getClassrooms(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.getClassroomsForUser(user));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ClassroomResponseDTO> inviteUser(
            @PathVariable UUID id,
            @RequestParam String username,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(classroomService.inviteUser(id, username, user));
    }
}
