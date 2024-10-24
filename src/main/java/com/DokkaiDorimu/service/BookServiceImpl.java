package com.DokkaiDorimu.service;

import com.DokkaiDorimu.exception.UnauthorizedException;
import com.DokkaiDorimu.repository.BookRepository;
import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.DTO.BookDTO;
import com.DokkaiDorimu.entity.Book;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.exception.BookNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        User currentUser = getCurrentUser();
        Book book = convertToEntity(bookDTO, currentUser);
        Book savedBook = bookRepository.save(book);
        return convertToDTO(savedBook);
    }
    @Override
    public BookDTO saveBook(BookDTO bookDTO, MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublisher(bookDTO.getPublisher());
        book.setPublishedDate(bookDTO.getPublishedDate());
        book.setDescription(bookDTO.getDescription());
        book.setGenre(bookDTO.getGenre());

        if (image != null && !image.isEmpty()) {
            try {
                String imgUrl = saveImage(image);
                book.setImageUrl(imgUrl);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        book.setUser(currentUser);


        Book savedBook = bookRepository.save(book);
        BookDTO savedBookDTO = convertToDTO(savedBook);
        savedBookDTO.setUsername(currentUser.getUsername());

        return savedBookDTO;
    }

    private String saveImage(MultipartFile file) throws IOException {
        String folder = "src/main/resources/static/images/";
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = folder + file.getOriginalFilename();
        Path path = Paths.get(filePath);
        Files.write(path, file.getBytes());

        return "/images/" + file.getOriginalFilename(); // Return the path to the stored image
    }

    @Override
    public BookDTO updateBook(Long id, BookDTO updatedBookDTO, MultipartFile image) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingBook.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("You are not allowed to edit this book");
        }

        existingBook.setTitle(updatedBookDTO.getTitle());
        existingBook.setAuthor(updatedBookDTO.getAuthor());
        existingBook.setPublisher(updatedBookDTO.getPublisher());
        existingBook.setPublishedDate(updatedBookDTO.getPublishedDate());
        existingBook.setDescription(updatedBookDTO.getDescription());
        existingBook.setGenre(updatedBookDTO.getGenre());

        // Handle image update
        if (image != null && !image.isEmpty()) {
            try {
                // Delete old image if it exists
                if (existingBook.getImageUrl() != null) {
                    deleteImage(existingBook.getImageUrl());
                }

                String imgUrl = saveImage(image);
                existingBook.setImageUrl(imgUrl);
            } catch (Exception e) {
                System.out.println("Error updating image: " + e.getMessage());
                // You might want to throw an exception here or handle it as per your error handling strategy
            }
        }

        Book savedBook = bookRepository.save(existingBook);
        BookDTO savedBookDTO = convertToDTO(savedBook);
        savedBookDTO.setAuthorImageUrl(currentUser.getImgUrl());  // Set the author's image URL

        return savedBookDTO;
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String folder = "src/main/resources/static";
            File file = new File(folder + imageUrl);
            if (file.exists()) {
                file.delete();
            }
        }
    }



    @Override
    public void deleteBook(Long id) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));

        User currentUser = getCurrentUser();
        if (!existingBook.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new UnauthorizedException("You are not allowed to delete this book");
        }

        bookRepository.deleteById(id);
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found"));
        return convertToDTO(book);
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public List<BookDTO> searchByGenre(String genre) {
        return bookRepository.findByGenre(genre).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Book convertToEntity(BookDTO bookDTO, User currentUser) {
        return new Book(
                null,
                bookDTO.getTitle(),
                bookDTO.getAuthor(),
                bookDTO.getPublisher(),
                bookDTO.getPublishedDate(),
                bookDTO.getDescription(),
                bookDTO.getGenre(),
                bookDTO.getImageUrl(),
                currentUser
        );
    }

    private BookDTO convertToDTO(Book book) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getDescription(),
                book.getGenre(),
                book.getImageUrl(),
                book.getUser().getUsername(),
                book.getUser().getImgUrl()
        );
    }
}