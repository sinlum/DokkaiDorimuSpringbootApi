package com.DokkaiDorimu.controller;

import com.DokkaiDorimu.exception.UnauthorizedException;
import com.DokkaiDorimu.DTO.BookDTO;
import com.DokkaiDorimu.exception.BookNotFoundException;
import com.DokkaiDorimu.service.BookService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BookDTO> uploadBook(@ModelAttribute BookDTO bookDTO,
                                              @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            BookDTO savedBook = bookService.saveBook(bookDTO, image);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BookDTO()); // or some error DTO
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable Long id,
            @ModelAttribute BookDTO bookDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            BookDTO updatedBook = bookService.updateBook(id, bookDTO, image);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (BookNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CREATOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (BookNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        try {
            BookDTO book = bookService.getBookById(id);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (BookNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
    @GetMapping("/searchByGenre")
    public ResponseEntity<List<BookDTO>> searchByGenre(@RequestParam String genre) {
        return ResponseEntity.ok(bookService.searchByGenre(genre));
    }
}