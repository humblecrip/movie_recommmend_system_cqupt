package com.controller;

import com.entity.DianyingleixingEntity;
import com.service.AppMovieCompatibilityService;
import com.service.CommonService;
import com.service.ConfigService;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonControllerCompatibilityTest {

    private static final String[] REMAINING_OFFLINING_CANDIDATES = {
            "dianyingdingdan",
            "dianyingfenlei",
            "discussfufeidianying",
            "discussmianfeidianying",
            "forum",
            "fufeidianying",
            "mianfeidianying",
            "news",
            "wodedianying",
            "schema_refactor_migration_log"
    };

    @Mock
    private CommonService commonService;

    @Mock
    private ConfigService configService;

    @Mock
    private AppMovieCompatibilityService appMovieCompatibilityService;

    @Test
    void getOptionShouldReadMovieTypesFromAppTables() {
        CommonController controller = newController();
        when(appMovieCompatibilityService.listLegacyTypeNames()).thenReturn(Arrays.asList("科幻", "悬疑"));

        R result = controller.getOption("dianyingleixing", "dianyingleixing", null, null, null, null);

        assertEquals(0, result.get("code"));
        assertEquals(Arrays.asList("科幻", "悬疑"), result.get("data"));
        verify(appMovieCompatibilityService).listLegacyTypeNames();
        verifyNoInteractions(commonService);
    }

    @Test
    void getOptionShouldRejectLegacyMovieTypeWhenTableNameUsesDifferentCase() {
        CommonController controller = newController();

        R result = controller.getOption("Dianyingleixing", "dianyingleixing", null, null, null, null);

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: dianyingleixing，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void getOptionShouldRejectLegacyMovieTypeWhenNamesContainSurroundingWhitespace() {
        CommonController controller = newController();

        R result = controller.getOption(" dianyingleixing ", " dianyingleixing ", null, null, null, null);

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: dianyingleixing，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void getFollowByOptionShouldReadMovieTypeDetailFromAppTables() {
        CommonController controller = newController();
        DianyingleixingEntity entity = new DianyingleixingEntity();
        entity.setId(-9L);
        entity.setDianyingleixing("科幻");

        when(appMovieCompatibilityService.selectLegacyMovieType(org.mockito.ArgumentMatchers.<DianyingleixingEntity<?>>any()))
                .thenReturn(entity);

        R result = controller.getFollowByOption("dianyingleixing", "dianyingleixing", "科幻");

        assertEquals(0, result.get("code"));
        assertEquals(entity, result.get("data"));
        verify(appMovieCompatibilityService).selectLegacyMovieType(org.mockito.ArgumentMatchers.<DianyingleixingEntity<?>>any());
        verifyNoInteractions(commonService);
    }

    @Test
    void getFollowByOptionShouldRejectLegacyMovieTypeWhenTableNameContainsWhitespace() {
        CommonController controller = newController();

        R result = controller.getFollowByOption(" dianyingleixing ", "dianyingleixing", "科幻");

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: dianyingleixing，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void groupShouldRejectLegacyMovieTableInsteadOfPassingRawTableName() {
        CommonController controller = newController();

        R result = controller.group("dianyingxinxi", "dianyingleixing");

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: dianyingxinxi，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void groupShouldRejectLegacyMovieTableWhenTableNameUsesDifferentCase() {
        CommonController controller = newController();

        R result = controller.group("Dianyingxinxi", "dianyingleixing");

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: dianyingxinxi，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void groupShouldRejectLegacyMovieTableWhenTableNameHasSurroundingWhitespace() {
        CommonController controller = newController();

        R result = controller.group(" dianyingxinxi ", "dianyingleixing");

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: dianyingxinxi，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void remindShouldRejectLegacyConfigTableInsteadOfPassingRawTableName() {
        CommonController controller = newController();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("remindstart", "0");

        R result = controller.remindCount("config", "id", "1", params);

        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: config，请改用兼容控制器或新接口", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void groupShouldRejectRemainingOffliningCandidates() {
        CommonController controller = newController();
        for (String tableName : REMAINING_OFFLINING_CANDIDATES) {
            assertLegacyBlocked(controller.group(tableName, "id"), tableName);
        }
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void optionAndFollowShouldRejectRemainingOffliningCandidates() {
        CommonController controller = newController();

        for (String tableName : REMAINING_OFFLINING_CANDIDATES) {
            assertLegacyBlocked(controller.getOption(tableName, "id", null, null, null, null), tableName);
            assertLegacyBlocked(controller.getFollowByOption(tableName, "id", "1"), tableName);
        }
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void dynamicWriteAndAggregationEndpointsShouldRejectRemainingOffliningCandidates() {
        CommonController controller = newController();

        for (String tableName : REMAINING_OFFLINING_CANDIDATES) {
            Map<String, Object> remindParams = new HashMap<String, Object>();
            remindParams.put("remindstart", "0");
            Map<String, Object> shPayload = new HashMap<String, Object>();
            shPayload.put("id", 1L);
            shPayload.put("sfsh", "是");

            assertLegacyBlocked(controller.cal(tableName, "id"), tableName);
            assertLegacyBlocked(controller.value(tableName, "clicknum", "addtime"), tableName);
            assertLegacyBlocked(controller.valueDay(tableName, "clicknum", "addtime", "月"), tableName);
            assertLegacyBlocked(controller.remindCount(tableName, "id", "1", remindParams), tableName);
            assertLegacyBlocked(controller.sh(tableName, shPayload), tableName);
        }
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void groupShouldPassResolvedRuntimeTableForNonLegacyRequest() {
        CommonController controller = newController();
        when(commonService.selectGroup(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(Collections.singletonList(Collections.<String, Object>singletonMap("total", 1)));

        R result = controller.group("app_movie", "movie_status");

        assertEquals(0, result.get("code"));
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(commonService).selectGroup(captor.capture());
        assertEquals("app_movie", captor.getValue().get("validatedTableName"));
        assertEquals("movie_status", captor.getValue().get("validatedColumnName"));
        assertNull(captor.getValue().get("table"));
        assertNull(captor.getValue().get("runtimeTable"));
        assertNull(captor.getValue().get("column"));
    }

    @Test
    void getOptionShouldPassValidatedIdentifiersForNonLegacyRequest() {
        CommonController controller = newController();
        when(commonService.getOption(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(Collections.singletonList("科技"));

        R result = controller.getOption("app_movie", "movie_status", "movie_status", "published", null, null);

        assertEquals(0, result.get("code"));
        assertEquals(Collections.singletonList("科技"), result.get("data"));
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(commonService).getOption(captor.capture());
        assertEquals("app_movie", captor.getValue().get("validatedTableName"));
        assertEquals("movie_status", captor.getValue().get("validatedColumnName"));
        assertEquals("movie_status", captor.getValue().get("validatedConditionColumnName"));
        assertNull(captor.getValue().get("runtimeTable"));
        assertNull(captor.getValue().get("column"));
        assertNull(captor.getValue().get("conditionColumn"));
    }

    @Test
    void groupShouldRejectUnsafeNonLegacyTableIdentifier() {
        CommonController controller = newController();

        R result = controller.group("NewsTable", "category");

        assertEquals(500, result.get("code"));
        assertEquals("动态入口仅允许非 legacy 且符合小写下划线格式的标识: tableName=NewsTable", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    @Test
    void valueDayShouldRejectUnsupportedTimeStatType() {
        CommonController controller = newController();

        R result = controller.valueDay("app_movie", "click_count", "addtime", "周");

        assertEquals(500, result.get("code"));
        assertEquals("动态时间统计类型不在允许范围: 周", result.get("msg"));
        verifyNoInteractions(commonService, appMovieCompatibilityService);
    }

    private CommonController newController() {
        CommonController controller = new CommonController();
        ReflectionTestUtils.setField(controller, "commonService", commonService);
        ReflectionTestUtils.setField(controller, "configService", configService);
        ReflectionTestUtils.setField(controller, "appMovieCompatibilityService", appMovieCompatibilityService);
        return controller;
    }

    private void assertLegacyBlocked(R result, String tableName) {
        assertEquals(500, result.get("code"));
        assertEquals("旧表动态入口已封禁: " + tableName + "，请改用兼容控制器或新接口", result.get("msg"));
    }
}
