package com.DokkaiDorimu.DTO;

import com.DokkaiDorimu.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingStatisticsDTO {
    private long totalArticlesRead;
    private Map<String, Long> categoryBreakdown;
    private long currentStreak;
    private List<RecentArticleDTO> recentlyRead;
}
