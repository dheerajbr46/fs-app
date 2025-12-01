package com.sb.booknetwork.service;

import com.sb.booknetwork.dto.PageResponse;
import com.sb.booknetwork.dto.feedback.FeedbackRequest;
import com.sb.booknetwork.dto.feedback.FeedbackResponse;
import com.sb.booknetwork.entity.book.Book;
import com.sb.booknetwork.entity.feedback.Feedback;
import com.sb.booknetwork.entity.user.User;
import com.sb.booknetwork.exception.OperationNotPermittedException;
import com.sb.booknetwork.repository.BookRepository;
import com.sb.booknetwork.repository.FeedbackRepository;
import com.sb.booknetwork.repository.mapper.FeedbackMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;

    public Integer saveFeedback(FeedbackRequest feedbackRequest, Authentication connectedUser) {
        Book book = bookRepository.findById(feedbackRequest.bookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + feedbackRequest.bookId()));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("Cannot leave feedback on archived or non-shareable books.");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("Owners cannot leave feedback on their own books.");
        }

        Feedback feedback = feedbackMapper.toFeedback(feedbackRequest);

        return feedbackRepository.save(feedback).getId();
    }

    public PageResponse<FeedbackResponse> findAllFeedbacksByBookId(Integer bookId, Integer page,
                                                                   Integer size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size);
        User user = (User) connectedUser.getPrincipal();

        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);
        List<FeedbackResponse> feedbackResponseList = feedbacks.stream()
                .map(feedback -> feedbackMapper.toFeedbackResponse(feedback, user.getId()))
                .toList();
        return new PageResponse<>(
                feedbackResponseList,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }
}
