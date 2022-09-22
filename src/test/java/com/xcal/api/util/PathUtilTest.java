/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.xcal.api.util;

import com.xcal.api.exception.UnexpectedPrefixException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void getPathDepth_FirstLevel_ReturnOne() {
        int result = PathUtil.getPathDepth("a");
        assertEquals(1, result);
    }

    @Test
    void getPathDepth_SecondLevel_ReturnTwo() {
        int result = PathUtil.getPathDepth("a/b");
        assertEquals(2, result);
    }

    @Test
    void getPathDepth_SecondLevelWithAbsolutePath_ReturnTwo() {
        int result = PathUtil.getPathDepth("/a/b");
        assertEquals(2, result);
    }

    @Test
    void getPathDepth_FirstLevelInWindows_ReturnOne() {
        int result = PathUtil.getPathDepth("c:\\");
        assertEquals(1, result);
    }

    @Test
    void getPathDepth_SecondLevelInWindows_ReturnTwo() {
        int result = PathUtil.getPathDepth("c:\\a");
        assertEquals(2, result);
    }


    @Test
    void getParentPath_inputRelativePath_ReturnRelativePath() {
        Optional<String> result = PathUtil.getParentPath("a/b/c");
        assertEquals("a/b", result.get());
    }

    @Test
    void getParentPath_inputAbsolutePath_ReturnAbsolutePath() {
        Optional<String> result = PathUtil.getParentPath("/a/b/c");
        assertEquals("/a/b", result.get());
    }

    @Test
    void getParentPath_inputRelativePathInWindows_ReturnRelativePath() {
        Optional<String> result = PathUtil.getParentPath("a\\b\\c");
        assertEquals("a\\b", result.get());
    }

    @Test
    void getParentPath_inputAbsolutePathInWindows_ReturnAbsolutePath() {
        Optional<String> result = PathUtil.getParentPath("c:\\a\\b\\");
        assertEquals("c:\\a", result.get());
    }

    @Test
    void getParentPath_inputRoot_ReturnAbsolutePath() {
        Optional<String> result = PathUtil.getParentPath("/");
        assertFalse(result.isPresent());
    }

    @Test
    void getPathCategory_hostPath_returnHost() throws UnexpectedPrefixException {
        String pathCategory=PathUtil.getPathCategory("$h/abcd");
        assertEquals("H",pathCategory);
    }

    @Test
    void getPathCategory_targetPath_returnTarget()  throws UnexpectedPrefixException {
        String pathCategory=PathUtil.getPathCategory("$t/abcd");
        assertEquals("T",pathCategory);
    }

    @Test
    void getPathCategory_rootPath_returnRoot()  throws UnexpectedPrefixException {
        String pathCategory=PathUtil.getPathCategory("/abcd");
        assertEquals("R",pathCategory);
    }

    @Test
    void getPathCategory_emptyString_returnEmptyString()  throws UnexpectedPrefixException {
        assertThrows(UnexpectedPrefixException.class,()->{PathUtil.getPathCategory("");});
    }

    @Test
    void getPathCategory_relativePath_returnEmptyString() {
        assertThrows(UnexpectedPrefixException.class,()->{PathUtil.getPathCategory("abc/def/ghi");});
    }

}
