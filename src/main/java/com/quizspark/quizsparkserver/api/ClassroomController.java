package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.ClassroomCreateDTO;
import com.quizspark.quizsparkserver.dto.ClassroomResponseDTO;
import com.quizspark.quizsparkserver.services.ClassroomService;
import org.springframework.http.ResponseEntity;
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
            @RequestBody ClassroomCreateDTO createDTO) {
        return ResponseEntity.ok(classroomService.createClassroom(createDTO));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomResponseDTO>> getClassrooms() {
        return ResponseEntity.ok(classroomService.getClassrooms());
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ClassroomResponseDTO> inviteUser(
            @PathVariable UUID id,
            @RequestParam String username) {
        return ResponseEntity.ok(classroomService.inviteUser(id, username));
    }
}
