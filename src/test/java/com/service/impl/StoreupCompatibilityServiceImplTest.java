package com.service.impl;

import com.dao.AppMovieInteractionDao;
import com.dao.StoreupCompatibilityDao;
import com.entity.StoreupEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreupCompatibilityServiceImplTest {

    @Mock
    private StoreupCompatibilityDao storeupCompatibilityDao;

    @Mock
    private AppMovieInteractionDao appMovieInteractionDao;

    @InjectMocks
    private StoreupCompatibilityServiceImpl service;

    @Test
    void saveShouldRejectNonMovieLegacyTargets() {
        StoreupEntity storeup = new StoreupEntity();
        storeup.setUserid(12L);
        storeup.setRefid(9L);
        storeup.setTablename("users");

        Long result = service.save(storeup, 12L);

        assertNull(result);
        verifyNoInteractions(storeupCompatibilityDao);
        verifyNoInteractions(appMovieInteractionDao);
    }

    @Test
    void saveShouldInsertMappedMovieActionWithResolvedAppUser() {
        StoreupEntity storeup = new StoreupEntity();
        storeup.setUserid(12L);
        storeup.setRefid(9L);
        storeup.setTablename("dianyingxinxi");
        storeup.setType("1");
        storeup.setName("Movie A");
        storeup.setPicture("poster-a.jpg");

        when(storeupCompatibilityDao.selectMovieIdByLegacyRefId(9L)).thenReturn(101L);
        when(appMovieInteractionDao.selectAppUserIdByLegacyUserId(12L)).thenReturn(202L);
        when(storeupCompatibilityDao.insertAction(12L, 202L, 101L, 9L, "dianyingxinxi",
                "favorite", "Movie A", "poster-a.jpg", null, null)).thenReturn(1);
        when(storeupCompatibilityDao.selectLastInsertId()).thenReturn(88L);

        Long result = service.save(storeup, 12L);

        assertEquals(88L, result);
        verify(storeupCompatibilityDao).insertAction(12L, 202L, 101L, 9L, "dianyingxinxi",
                "favorite", "Movie A", "poster-a.jpg", null, null);
    }

    @Test
    void updateShouldRejectUnsafeMovieMapping() {
        StoreupEntity storeup = new StoreupEntity();
        storeup.setId(77L);
        storeup.setUserid(12L);
        storeup.setRefid(9L);
        storeup.setTablename("users");

        boolean updated = service.update(storeup, 12L, false);

        assertFalse(updated);
        verifyNoInteractions(storeupCompatibilityDao);
        verifyNoInteractions(appMovieInteractionDao);
    }
}
