package com.hirwa.classprogram.classroom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomRestController {

    @GetMapping("/public")
    public List<Classroom> getPublicClassrooms() {
        
        return classroomService.getPublicClassrooms();
    }

    private final ClassroomService classroomService;

    public ClassroomRestController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }
}

