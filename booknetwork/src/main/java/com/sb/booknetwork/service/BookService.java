package com.sb.booknetwork.service;

import com.sb.booknetwork.dto.PageResponse;
import com.sb.booknetwork.dto.book.BookRequest;
import com.sb.booknetwork.dto.book.BookResponse;
import com.sb.booknetwork.dto.book.BorrowedBookResponse;
import com.sb.booknetwork.entity.book.Book;
import com.sb.booknetwork.entity.book.BookTransactionHistory;
import com.sb.booknetwork.entity.user.User;
import com.sb.booknetwork.exception.OperationNotPermittedException;
import com.sb.booknetwork.repository.BookRepository;
import com.sb.booknetwork.repository.BookTransactionHistoryRepository;
import com.sb.booknetwork.repository.mapper.BookMapper;
import com.sb.booknetwork.repository.specification.BookSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final FileStorageService fileStorageService;

    public Integer saveBook(BookRequest bookRequest, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Book book = bookMapper.toBook(bookRequest);
        book.setOwner(user);

        return bookRepository.save(book).getId();
    }

    public BookResponse findBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
        List<BookResponse> bookResponseList = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponseList,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);
        List<BookResponse> bookResponseList = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponseList,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> borrowedList = bookTransactionHistoryRepository.findAllBorrowedBooks(user.getId(), pageable);
        List<BorrowedBookResponse> borrowedBookResponses = borrowedList.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                borrowedBookResponses,
                borrowedList.getNumber(),
                borrowedList.getSize(),
                borrowedList.getTotalElements(),
                borrowedList.getTotalPages(),
                borrowedList.isFirst(),
                borrowedList.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> borrowedList = bookTransactionHistoryRepository.findAllReturnedBooks(user.getId(), pageable);
        List<BorrowedBookResponse> borrowedBookResponses = borrowedList.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                borrowedBookResponses,
                borrowedList.getNumber(),
                borrowedList.getSize(),
                borrowedList.getTotalElements(),
                borrowedList.getTotalPages(),
                borrowedList.isFirst(),
                borrowedList.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
        User user = (User) connectedUser.getPrincipal();

        if (!Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You are not authorized to update this book's shareable status.");
        }

        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
        User user = (User) connectedUser.getPrincipal();

        if (!Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You are not authorized to update this book's archived status.");
        }

        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("This book is not available for borrowing.");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book.");
        }

        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository
                .isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("This book is already borrowed and not yet returned.");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .book(book)
                .user(user)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("This book is not available for borrowing.");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You cannot borrow/return your own book.");
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You have not borrowed this book."));
        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("This book is not available for borrowing.");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(user.getId(), book.getOwner().getId())) {
            throw new OperationNotPermittedException("You cannot borrow/return your own book.");
        }

        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("This book is not returned yet."));
        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public void uploadBookCover(Integer bookId, MultipartFile file, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));

        User user = (User) connectedUser.getPrincipal();
        var bookCover = fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
