package com.service;

import com.SpringbootSchemaApplication;
import com.entity.vo.AppMovieDashboardOverviewVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = SpringbootSchemaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@Rollback
class AppMovieDashboardOverviewIntegrationTest {

    @Autowired
    private AppMovieService appMovieService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getDashboardOverviewShouldExposeCurrentRuntimeInteractionTotals() {
        AppMovieDashboardOverviewVO overview = appMovieService.getDashboardOverview();

        long favoriteCount = queryLong("SELECT COUNT(1) FROM app_user_movie_action WHERE action_type = 'favorite'");
        long likeCount = queryLong("SELECT COUNT(1) FROM app_user_movie_action WHERE action_type = 'like'");
        long dislikeCount = queryLong("SELECT COUNT(1) FROM app_user_movie_action WHERE action_type = 'dislike'");
        long commentCount = queryLong("SELECT COUNT(1) FROM app_movie_comment");
        long snapshotFavoriteTotal = queryLong("SELECT COALESCE(SUM(favorite_count), 0) FROM app_movie");
        long snapshotLikeTotal = queryLong("SELECT COALESCE(SUM(like_count), 0) FROM app_movie");
        long snapshotDislikeTotal = queryLong("SELECT COALESCE(SUM(dislike_count), 0) FROM app_movie");
        long snapshotCommentTotal = queryLong("SELECT COALESCE(SUM(comment_count), 0) FROM app_movie");

        System.out.println(
                "ACTUAL_OVERVIEW_TOTALS favorite=" + overview.getTotalFavoriteCount()
                        + " like=" + overview.getTotalLikeCount()
                        + " dislike=" + overview.getTotalDislikeCount()
                        + " comment=" + overview.getTotalCommentCount()
        );
        System.out.println(
                "ACTUAL_DB_TOTALS favorite=" + favoriteCount
                        + " like=" + likeCount
                        + " dislike=" + dislikeCount
                        + " comment=" + commentCount
        );
        System.out.println(
                "LEGACY_SNAPSHOT_TOTALS favorite=" + snapshotFavoriteTotal
                        + " like=" + snapshotLikeTotal
                        + " dislike=" + snapshotDislikeTotal
                        + " comment=" + snapshotCommentTotal
        );

        assertEquals(favoriteCount, overview.getTotalFavoriteCount().longValue());
        assertEquals(likeCount, overview.getTotalLikeCount().longValue());
        assertEquals(dislikeCount, overview.getTotalDislikeCount().longValue());
        assertEquals(commentCount, overview.getTotalCommentCount().longValue());
    }

    private long queryLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
