package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.BookDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    BookDTO addBook(BookDTO bookDTO);
    BookDTO saveBook(BookDTO bookDTO, MultipartFile image);
    BookDTO updateBook(Long id, BookDTO bookDTO, MultipartFile image);
    void deleteBook(Long id);
    BookDTO getBookById(Long id);
    List<BookDTO> getAllBooks();
    List<BookDTO> searchByGenre(String genre);
}