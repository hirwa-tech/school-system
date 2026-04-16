package com.hirwa.classprogram.quiz.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {

    private Long id;
    private String questionText;
    private List<String> options;
    private String type;
}