package com.service;

import com.entity.DianyingleixingEntity;
import com.entity.DianyingxinxiEntity;
import com.utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 电影/类型旧 URL 到 app_* 新表的兼容桥接服务。
 */
public interface AppMovieCompatibilityService {

    PageUtils queryLegacyMoviePage(Map<String, Object> params, DianyingxinxiEntity<?> filter);

    List<DianyingxinxiEntity> selectLegacyMovieList(DianyingxinxiEntity<?> filter);

    DianyingxinxiEntity<?> selectLegacyMovie(DianyingxinxiEntity<?> filter);

    DianyingxinxiEntity<?> getLegacyMovieById(Long compatId, boolean incrementClick);

    Long saveLegacyMovie(DianyingxinxiEntity<?> movie);

    boolean updateLegacyMovie(DianyingxinxiEntity<?> movie);

    int deleteLegacyMovies(List<Long> compatIds);

    PageUtils queryLegacyRecommendedMovies(Map<String, Object> params, DianyingxinxiEntity<?> filter, Long legacyUserId);

    int countLegacyMovies(Map<String, Object> params, DianyingxinxiEntity<?> filter);

    List<Map<String, Object>> selectLegacyMovieValueStats(String xColumnName, String yColumnName, String timeStatType);

    List<Map<String, Object>> selectLegacyMovieGroupStats(String columnName);

    boolean incrementLegacyMovieVote(Long compatId, String type);

    PageUtils queryLegacyMovieTypePage(Map<String, Object> params, DianyingleixingEntity<?> filter);

    List<DianyingleixingEntity> selectLegacyMovieTypeList(DianyingleixingEntity<?> filter);

    DianyingleixingEntity<?> selectLegacyMovieType(DianyingleixingEntity<?> filter);

    DianyingleixingEntity<?> getLegacyMovieTypeById(Long compatId);

    Long saveLegacyMovieType(DianyingleixingEntity<?> movieType);

    boolean updateLegacyMovieType(DianyingleixingEntity<?> movieType);

    int deleteLegacyMovieTypes(List<Long> compatIds);

    List<String> listLegacyTypeNames();
}
