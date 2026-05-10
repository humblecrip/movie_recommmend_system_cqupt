package com.controller;

import com.service.SensitivewordsService;
import com.utils.R;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordsControllerTest {

    @Mock
    private SensitivewordsService sensitivewordsService;

    @Test
    void listShouldReturnNormalizedKeywordArray() {
        KeywordsController controller = new KeywordsController();
        ReflectionTestUtils.setField(controller, "sensitivewordsService", sensitivewordsService);
        when(sensitivewordsService.listNormalizedKeywords()).thenReturn(Arrays.asList("超长关键词", "短词"));

        R result = controller.list();

        assertEquals(0, result.get("code"));
        assertEquals(Arrays.asList("超长关键词", "短词"), result.get("data"));
        verify(sensitivewordsService).listNormalizedKeywords();
    }
}
