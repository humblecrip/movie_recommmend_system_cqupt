package com.controller;

import com.entity.DianyingleixingEntity;
import com.service.AppMovieCompatibilityService;
import com.service.DianyingleixingService;
import com.utils.PageUtils;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DianyingleixingControllerCompatibilityTest {

    @Mock
    private DianyingleixingService dianyingleixingService;

    @Mock
    private AppMovieCompatibilityService appMovieCompatibilityService;

    @Test
    void pageShouldUseCompatibilityServiceInsteadOfLegacyTypeTable() {
        DianyingleixingController controller = newController();
        Map<String, Object> params = new HashMap<String, Object>();
        DianyingleixingEntity<?> filter = new DianyingleixingEntity<Object>();
        PageUtils page = new PageUtils(Collections.singletonList(new DianyingleixingEntity<Object>()), 1, 10, 1);

        when(appMovieCompatibilityService.queryLegacyMovieTypePage(params, filter)).thenReturn(page);

        R result = controller.page(params, filter, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(page, result.get("data"));
        verify(appMovieCompatibilityService).queryLegacyMovieTypePage(params, filter);
        verifyNoInteractions(dianyingleixingService);
    }

    @Test
    void infoShouldUseCompatibilityService() {
        DianyingleixingController controller = newController();
        DianyingleixingEntity<Object> type = new DianyingleixingEntity<Object>();
        type.setId(-7L);
        type.setDianyingleixing("科幻");

        doReturn(type).when(appMovieCompatibilityService).getLegacyMovieTypeById(-7L);

        R result = controller.info(-7L);

        assertEquals(0, result.get("code"));
        assertEquals(type, result.get("data"));
        verify(appMovieCompatibilityService).getLegacyMovieTypeById(-7L);
        verifyNoInteractions(dianyingleixingService);
    }

    @Test
    void saveShouldUseCompatibilityServiceInsteadOfLegacyInsert() {
        DianyingleixingController controller = newController();
        DianyingleixingEntity<?> payload = new DianyingleixingEntity<Object>();
        payload.setDianyingleixing("悬疑");

        when(appMovieCompatibilityService.saveLegacyMovieType(payload)).thenReturn(-11L);

        R result = controller.save(payload, new MockHttpServletRequest());

        assertEquals(0, result.get("code"));
        assertEquals(-11L, result.get("data"));
        verify(appMovieCompatibilityService).saveLegacyMovieType(payload);
        verifyNoInteractions(dianyingleixingService);
    }

    @Test
    void deleteShouldUseCompatibilityServiceInsteadOfLegacyDeleteBatch() {
        DianyingleixingController controller = newController();
        Long[] ids = new Long[]{3L, -4L};

        when(appMovieCompatibilityService.deleteLegacyMovieTypes(java.util.Arrays.asList(ids))).thenReturn(2);

        R result = controller.delete(ids);

        assertEquals(0, result.get("code"));
        verify(appMovieCompatibilityService).deleteLegacyMovieTypes(java.util.Arrays.asList(ids));
        verifyNoInteractions(dianyingleixingService);
    }

    private DianyingleixingController newController() {
        DianyingleixingController controller = new DianyingleixingController();
        ReflectionTestUtils.setField(controller, "dianyingleixingService", dianyingleixingService);
        ReflectionTestUtils.setField(controller, "appMovieCompatibilityService", appMovieCompatibilityService);
        return controller;
    }
}
