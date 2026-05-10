package com.service.impl;

import com.dao.AppMovieCompatibilityDao;
import com.entity.DianyingxinxiEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppMovieCompatibilityServiceImplTest {

    @Mock
    private AppMovieCompatibilityDao appMovieCompatibilityDao;

    @InjectMocks
    private AppMovieCompatibilityServiceImpl service;

    @Test
    void selectLegacyMovieValueStatsShouldAggregateFromCompatibilityDao() {
        DianyingxinxiEntity<Object> movieA = new DianyingxinxiEntity<Object>();
        movieA.setDianyingleixing("科幻");
        movieA.setClicknum(10);

        DianyingxinxiEntity<Object> movieB = new DianyingxinxiEntity<Object>();
        movieB.setDianyingleixing("科幻");
        movieB.setClicknum(8);

        DianyingxinxiEntity<Object> movieC = new DianyingxinxiEntity<Object>();
        movieC.setDianyingleixing("剧情");
        movieC.setClicknum(3);

        when(appMovieCompatibilityDao.selectLegacyMovies(null, null, null, null, null, "m.id", "asc"))
                .thenReturn(Arrays.asList(movieA, movieB, movieC));

        List<Map<String, Object>> result = service.selectLegacyMovieValueStats("dianyingleixing", "clicknum", null);

        assertEquals(2, result.size());
        assertEquals("科幻", result.get(0).get("dianyingleixing"));
        assertEquals(18L, result.get(0).get("total"));
        assertEquals("剧情", result.get(1).get("dianyingleixing"));
        assertEquals(3L, result.get(1).get("total"));
        verify(appMovieCompatibilityDao).selectLegacyMovies(null, null, null, null, null, "m.id", "asc");
    }

    @Test
    void selectLegacyMovieValueStatsShouldSupportTimeBucketsFromAppMovieData() throws Exception {
        DianyingxinxiEntity<Object> movieA = new DianyingxinxiEntity<Object>();
        movieA.setShangyingshijian(new SimpleDateFormat("yyyy-MM-dd").parse("2026-04-03"));
        movieA.setStoreupnum(5);

        DianyingxinxiEntity<Object> movieB = new DianyingxinxiEntity<Object>();
        movieB.setShangyingshijian(new SimpleDateFormat("yyyy-MM-dd").parse("2026-04-20"));
        movieB.setStoreupnum(7);

        when(appMovieCompatibilityDao.selectLegacyMovies(null, null, null, null, null, "m.id", "asc"))
                .thenReturn(Arrays.asList(movieA, movieB));

        List<Map<String, Object>> result = service.selectLegacyMovieValueStats("shangyingshijian", "storeupnum", "月");

        assertEquals(1, result.size());
        assertEquals("2026-04", result.get(0).get("shangyingshijian"));
        assertEquals(12L, result.get(0).get("total"));
    }

    @Test
    void selectLegacyMovieGroupStatsShouldCountFromCompatibilityDao() {
        DianyingxinxiEntity<Object> movieA = new DianyingxinxiEntity<Object>();
        movieA.setDianyingleixing("动作");

        DianyingxinxiEntity<Object> movieB = new DianyingxinxiEntity<Object>();
        movieB.setDianyingleixing("动作");

        DianyingxinxiEntity<Object> movieC = new DianyingxinxiEntity<Object>();
        movieC.setDianyingleixing("悬疑");

        when(appMovieCompatibilityDao.selectLegacyMovies(null, null, null, null, null, "m.id", "asc"))
                .thenReturn(Arrays.asList(movieA, movieB, movieC));

        List<Map<String, Object>> result = service.selectLegacyMovieGroupStats("dianyingleixing");

        assertEquals(2, result.size());
        assertEquals("动作", result.get(0).get("dianyingleixing"));
        assertEquals(2L, result.get(0).get("total"));
        assertEquals("悬疑", result.get(1).get("dianyingleixing"));
        assertEquals(1L, result.get(1).get("total"));
    }
}
