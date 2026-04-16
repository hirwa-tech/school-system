package com.hirwa.classprogram.quiz.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizDto {

    private Long id;
    private String title;
    private String description;
    private Long classroomId;
    private Long teacherId;
    private List<QuestionDto> questions;
    private Integer timeLimit;
    private boolean antiCheatEnabled;
}