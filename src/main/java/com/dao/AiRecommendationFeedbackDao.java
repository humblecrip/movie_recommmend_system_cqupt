package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.entity.AiRecommendationFeedbackEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI Recommendation Feedback DAO.
 */
public interface AiRecommendationFeedbackDao extends BaseMapper<AiRecommendationFeedbackEntity> {

    List<Long> selectPositiveMovieIds(@Param("userId") Long userId);

    List<Long> selectNegativeMovieIds(@Param("userId") Long userId);
}
