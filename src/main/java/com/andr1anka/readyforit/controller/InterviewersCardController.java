package com.andr1anka.readyforit.controller;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import com.andr1anka.readyforit.service.InterviewerCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/interviewers")
public class InterviewersCardController {
    private final InterviewerCardService interviewerCardService;
    public InterviewersCardController(InterviewerCardService interviewerCardService) {
        this.interviewerCardService = interviewerCardService;
    }
    @GetMapping
    public ResponseEntity<List<InterviewerCardDTO>> getAllCards(){
       log.info("Getting all cards with lessons");

       return ResponseEntity.status(HttpStatus.OK).body(
               interviewerCardService.getAllCards());
    }


}
