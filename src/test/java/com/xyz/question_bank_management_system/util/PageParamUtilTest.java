package com.xyz.question_bank_management_system.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageParamUtilTest {

    @Test
    void normalizePage_shouldUseDefaultWhenInvalid() {
        assertEquals(1L, PageParamUtil.normalizePage(0));
        assertEquals(1L, PageParamUtil.normalizePage(-5));
    }

    @Test
    void normalizeSize_shouldUseDefaultAndCapMax() {
        assertEquals(20L, PageParamUtil.normalizeSize(0));
        assertEquals(20L, PageParamUtil.normalizeSize(-1));
        assertEquals(200L, PageParamUtil.normalizeSize(999));
    }

    @Test
    void offset_shouldUseNormalizedValues() {
        assertEquals(0L, PageParamUtil.offset(0, 0));
        assertEquals(20L, PageParamUtil.offset(2, 20));
        assertEquals(400L, PageParamUtil.offset(3, 200));
    }
}
