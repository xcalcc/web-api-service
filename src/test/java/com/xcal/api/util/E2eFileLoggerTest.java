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

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
class E2eFileLoggerTest {

    @MockBean
    FileWriter fw;

    @MockBean
    BufferedWriter bw;

    @Test
    void writeLine_normal_success() throws IOException {

//        FileWriter fw = new FileWriter("") {
//
//        };
//        BufferedWriter bw = new BufferedWriter(fw) {
//
//        };

        doNothing().when(bw).write(anyString());

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.writeLine("Content");

    }

    @Test
    void writeLine_IOException_ignore() throws IOException {

        doThrow(new IOException("Test")).when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.writeLine("Content");
    }

    @Test
    void writeLine_null_skip() throws IOException {

        doNothing().when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.writeLine(null);
    }

    @Test
    void write_IOException_success() throws IOException {

        doNothing().when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.write("Content");
    }


    @Test
    void write_IOException_ignore() throws IOException {

        doThrow(new IOException("Test")).when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.write("Content");
    }

    @Test
    void write_null_skip() throws IOException {

        doNothing().when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.write(null);
    }

    @Test
    void newLine_normal_success() throws IOException {

        doNothing().when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.newLine();
    }


    @Test
    void newLine_IOException_ignore() throws IOException {

        doThrow(new IOException("Test")).when(bw).write(anyString());
        doThrow(new IOException("Test")).when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.newLine();
    }

    @Test
    void close_normal_success() throws IOException {

        doNothing().when(bw).write(anyString());
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.close();
    }

    @Test
    void close_IOException_ignore() throws IOException {

        doThrow(new IOException("Test")).when(bw).close();
        doNothing().when(bw).newLine();

        String scanTaskId = "0001";
        E2EFileLogger e2EFileLogger = new E2EFileLogger(fw, bw, scanTaskId);
        e2EFileLogger.close();
    }

}