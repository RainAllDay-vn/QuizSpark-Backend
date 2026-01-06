package com.quizspark.quizsparkserver.api;

import com.quizspark.quizsparkserver.dto.classrooms.ClassroomCreateDTO;
import com.quizspark.quizsparkserver.dto.classrooms.ClassroomDTO;
import com.quizspark.quizsparkserver.dto.classrooms.ClassroomDetailsDTO;
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
    public ResponseEntity<ClassroomDTO> createClassroom(
            @RequestBody ClassroomCreateDTO createDTO) {
        return ResponseEntity.ok(classroomService.createClassroom(createDTO));
    }

    @GetMapping
    public ResponseEntity<List<ClassroomDTO>> getClassrooms() {
        return ResponseEntity.ok(classroomService.getClassrooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDetailsDTO> getClassroom(@PathVariable UUID id) {
        return ResponseEntity.ok(classroomService.getClassroomById(id));
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ClassroomDTO> inviteUser(
            @PathVariable UUID id,
            @RequestParam String username) {
        return ResponseEntity.ok(classroomService.inviteUser(id, username));
    }

    @PostMapping("/join")
    public ResponseEntity<ClassroomDTO> joinClassroom(@RequestParam String joinCode) {
        return ResponseEntity.ok(classroomService.joinClassroom(joinCode));
    }
}
