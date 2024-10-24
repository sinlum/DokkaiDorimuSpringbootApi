package com.DokkaiDorimu.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardArticlesDTO {
    private List<ArticleDTO> latestArticles;
    private List<ArticleDTO> hotArticles;
}
