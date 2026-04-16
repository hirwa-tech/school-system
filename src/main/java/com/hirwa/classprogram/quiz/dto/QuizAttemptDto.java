package com.hirwa.classprogram.quiz.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class QuizAttemptDto {

    private Long id;
    private Long quizId;
    private Long studentId;
    private Map<Long, String> answers;
    private Double score;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean flagged;
}