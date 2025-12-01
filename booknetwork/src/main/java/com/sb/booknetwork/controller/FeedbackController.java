package com.sb.booknetwork.controller;

import com.sb.booknetwork.dto.PageResponse;
import com.sb.booknetwork.dto.feedback.FeedbackRequest;
import com.sb.booknetwork.dto.feedback.FeedbackResponse;
import com.sb.booknetwork.service.FeedbackService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback Controller", description = "APIs for managing feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    private ResponseEntity<Integer> saveFeedback(
            @Valid
            @RequestBody
            FeedbackRequest feedbackRequest,

            Authentication connectedUser
    ) {
        return ResponseEntity.ok(feedbackService.saveFeedback(feedbackRequest, connectedUser));
    }

    @GetMapping("/book/{book-id}")
    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbacksByBookId(
            @PathVariable("book-id") Integer bookId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(feedbackService.findAllFeedbacksByBookId(bookId, page, size, connectedUser));
    }
}
