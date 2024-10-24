package com.DokkaiDorimu.service;

import com.DokkaiDorimu.DTO.BookmarkedArticleDTO;
import java.util.List;

public interface BookmarkService {
    void addBookmark(Long articleId);
    void removeBookmark(Long articleId);
    List<BookmarkedArticleDTO> getBookmarkedArticles();


}