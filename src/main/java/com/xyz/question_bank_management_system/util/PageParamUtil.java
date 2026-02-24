package com.xyz.question_bank_management_system.util;

public final class PageParamUtil {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 200L;

    private PageParamUtil() {
    }

    public static long normalizePage(long page) {
        return page > 0 ? page : DEFAULT_PAGE;
    }

    public static long normalizeSize(long size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public static long offset(long page, long size) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        return (safePage - 1) * safeSize;
    }
}
