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

package com.xcal.api.model;

import com.xcal.api.exception.AppException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

class AppExceptionTest extends Exception {

    private static final String LEVEL_DEFAULT = AppException.LEVEL_ERROR;
    private static final String LEVEL = AppException.LEVEL_WARN;
    private static final String ERROR_CODE = "DATA_NOT_FOUND";
    private static final int RESPONSE_CODE = HttpURLConnection.HTTP_NOT_FOUND;
    private static final String ERROR_MESSAGE = "Not Found";
    private static final String LOCALE_ERROR_MESSAGE = "Not Found-Locale";
    private static final IOException IOE = new IOException("IOException");

    /**
     * Test the all arg constructor.
     */
    @Test
    void testAllArgConstructor() {
        final AppException e = new AppException(LEVEL, ERROR_CODE, RESPONSE_CODE, null, ERROR_MESSAGE, IOE);
        Assertions.assertEquals(LEVEL, e.getLevel());
        Assertions.assertEquals(ERROR_CODE, e.getErrorCode());
        Assertions.assertEquals(Integer.valueOf(HttpURLConnection.HTTP_NOT_FOUND), e.getResponseCode());
        Assertions.assertEquals(ERROR_MESSAGE, e.getMessage());
        Assertions.assertEquals(IOE.getMessage(), e.getCause().getMessage());
    }

    /**
     * Test the no cause constructor.
     */
    @Test
    void testNoCauseConstructor() {
        final AppException e = new AppException(LEVEL, ERROR_CODE, RESPONSE_CODE, null, ERROR_MESSAGE);

        Assertions.assertEquals(LEVEL, e.getLevel());
        Assertions.assertEquals(ERROR_CODE, e.getErrorCode());
        Assertions.assertEquals(Integer.valueOf(HttpURLConnection.HTTP_NOT_FOUND), e.getResponseCode());
        Assertions.assertEquals(ERROR_MESSAGE, e.getMessage());
        Assertions.assertNull(e.getCause());
    }

    /**
     * Test the no level constructor.
     */
    @Test
    void testNoLevelConstructor() {
        final AppException e = new AppException(ERROR_CODE, RESPONSE_CODE, null, ERROR_MESSAGE, IOE);

        Assertions.assertEquals(LEVEL_DEFAULT, e.getLevel());
        Assertions.assertEquals(ERROR_CODE, e.getErrorCode());
        Assertions.assertEquals(Integer.valueOf(HttpURLConnection.HTTP_NOT_FOUND), e.getResponseCode());
        Assertions.assertEquals(ERROR_MESSAGE, e.getMessage());
        Assertions.assertEquals(IOE.getMessage(), e.getCause().getMessage());
    }

    @Test
    void getLocalizedMessage_emptyLocaleMessage() {
        final AppException e = new AppException(AppException.LEVEL_ERROR, ERROR_CODE, RESPONSE_CODE, AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.unifyErrorCode,
                ERROR_MESSAGE, null, IOE);
        Assertions.assertEquals(ERROR_MESSAGE, e.getLocalizedMessage());
    }

    @Test
    void getLocalizedMessage_existLocaleMessage() {
        final AppException e = new AppException(AppException.LEVEL_ERROR, ERROR_CODE, RESPONSE_CODE, AppException.ErrorCode.E_API_FILE_UPLOAD_FILE_FAILED.unifyErrorCode,
                ERROR_MESSAGE, LOCALE_ERROR_MESSAGE, IOE);
        Assertions.assertEquals(LOCALE_ERROR_MESSAGE, e.getLocalizedMessage());
    }
}
