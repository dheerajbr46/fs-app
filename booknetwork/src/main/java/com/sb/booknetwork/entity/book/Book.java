package com.sb.booknetwork.entity.book;

import com.sb.booknetwork.entity.common.BaseEntity;
import com.sb.booknetwork.entity.feedback.Feedback;
import com.sb.booknetwork.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Book extends BaseEntity {
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archived;
    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbackList;

    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @Transient
    public double getRate() {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return 0.0;
        }

        double sum = this.feedbackList.stream()
                .mapToDouble(Feedback::getNote)
                .average()
                .orElse(0.0);

        double roundedSum = Math.round(sum * 10.0) / 10.0;

        return roundedSum;
    }
}
