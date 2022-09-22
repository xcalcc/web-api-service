package com.xcal.api.util;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilTest {

    @Test
    public void wildCardMatch_emptyString_match() {
        assertEquals(true, StringUtil.wildCardMatch("", ""));
    }

    @Test
    public void wildCardMatch_emptyString_noMatch() {
        assertEquals(false, StringUtil.wildCardMatch("abcde", ""));
    }

    @Test
    public void wildCardMatch_questionMark_matchAnySingleChar() {
        assertEquals(true, StringUtil.wildCardMatch("a", "?"));
        assertEquals(true, StringUtil.wildCardMatch("z", "?"));
        assertEquals(true, StringUtil.wildCardMatch("1", "?"));
        assertEquals(true, StringUtil.wildCardMatch("9", "?"));
    }

    @Test
    public void wildCardMatch_asterisk_matchMoreThan1Char() {
        assertEquals(true, StringUtil.wildCardMatch("abcde", "*"));
    }

    @Test
    public void wildCardMatch_asterisk_matchAny1Char() {
        assertEquals(true, StringUtil.wildCardMatch("a", "*"));
        assertEquals(true, StringUtil.wildCardMatch("z", "*"));
        assertEquals(true, StringUtil.wildCardMatch("1", "*"));
        assertEquals(true, StringUtil.wildCardMatch("9", "*"));
    }

    @Test
    public void wildCardMatch_asterisk_matchEmptyString() {
        assertEquals(true, StringUtil.wildCardMatch("", "*"));
    }

    @Test
    public void wildCardMatch_asteriskStart_matchAnyStart() {
        assertEquals(true, StringUtil.wildCardMatch("aend", "*end"));
        assertEquals(true, StringUtil.wildCardMatch("zend", "*end"));
        assertEquals(true, StringUtil.wildCardMatch("1end", "*end"));
        assertEquals(true, StringUtil.wildCardMatch("9end", "*end"));
    }

    @Test
    public void wildCardMatch_asteriskEnd_matchAnyEnd() {
        assertEquals(true, StringUtil.wildCardMatch("starta", "start*"));
        assertEquals(true, StringUtil.wildCardMatch("startz", "start*"));
        assertEquals(true, StringUtil.wildCardMatch("start1", "start*"));
        assertEquals(true, StringUtil.wildCardMatch("start9", "start*"));
    }

    @Test
    public void wildCardMatch_asterisk_matchWithinFolder() {
        assertEquals(false, StringUtil.wildCardMatch("/a/b", "/*"));
        assertEquals(true, StringUtil.wildCardMatch("/a/b", "/*/b"));
    }

    @Test
    public void wildCardMatch_doubleAsterisk_matchAcrossFolders() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b", "/**"));
        assertEquals(true, StringUtil.wildCardMatch("/a/b", "/**/b"));
    }

    @Test
    public void wildCardMatch_doubleAsteriskStart_matchAcrossFolders() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c", "**/c"));
    }

    @Test
    public void wildCardMatch_doubleAsteriskEnd_matchAcrossFolders() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c", "/a/**"));
    }

    @Test
    public void wildCardMatch_doubleAsteriskWithQuestionMark_match() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c/d.h", "/a/**/?.h"));
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c/e.h", "/a/**/?.h"));
    }

    @Test
    public void wildCardMatch_doubleAsteriskWithQuestionMark_notMatch() {
        assertEquals(false, StringUtil.wildCardMatch("/a/b/c/d.h", "/a/**/?"));
        assertEquals(false, StringUtil.wildCardMatch("/a/b/c/e.h", "/a/**/?"));
    }

    @Test
    public void wildCardMatch_singleAsteriskWithQuestionMark_match() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c.h", "/a/*/?.h"));
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c.h", "/a/*/?.h"));
    }

    @Test
    public void wildCardMatch_singleAsteriskWithQuestionMark_notMatch() {
        assertEquals(false, StringUtil.wildCardMatch("/a/b/c.h", "/a/*/?"));
        assertEquals(false, StringUtil.wildCardMatch("/a/b/c.h", "/a/*/?"));
    }

    @Test
    public void wildCardMatch_singleAsteriskWithDoubleAsterisk_match() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c/d/e/f.h", "/a/**/*.h"));
    }

    @Test
    public void wildCardMatch_singleAsteriskWithDoubleAsterisk_notMatch() {
        assertEquals(false, StringUtil.wildCardMatch("/a/b/c/d/e/f.h", "/a/**/*/"));
    }

    @Test
    public void wildCardMatch_singleAsteriskWithDoubleAsteriskAndQuestionMark_match() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c/d/e/f.h", "**/d/*/?.h"));
    }

    @Test
    public void wildCardMatch_singleAsteriskWithDoubleAsteriskAndQuestionMark_notmatch() {
        assertEquals(true, StringUtil.wildCardMatch("/a/b/c/d/e/f.h", "**/d/*/?.h"));
    }

}

