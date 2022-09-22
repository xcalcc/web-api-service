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

package com.xcal.api.service;

import com.xcal.api.entity.FileInfo;
import com.xcal.api.entity.FileStorage;
import com.xcal.api.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class LocalFileServiceTest {
    private LocalFileService localFileService;

    private String currentUserName = "user";
    @BeforeEach
    void setUp() {
        localFileService = new LocalFileService();
    }

    @Test
    void getFileAsResourceTestSuccess() throws AppException, IOException {
        log.info("[getFileAsResourceTestSuccess]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("hellowold", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileInfo fileInfo = FileInfo.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111110")).fileStorage(fileStorage).relativePath(file.getName()).build();
        Resource resource = localFileService.getFileAsResource(fileInfo);
        assertEquals(file.length(), resource.getFile().length());
    }


    @Test
    void storeFileTestFileAlreadyExistFail() throws IOException {
        log.info("[storeFileTestFileAlreadyExistFail]");
        MockMultipartFile sourceFile = new MockMultipartFile("source_file",
                "test.c",
                "text/plain",
                "helloworld".getBytes());
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("hellowold", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileInfo fileInfo = FileInfo.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111110")).fileStorage(fileStorage).relativePath(file.getName()).build();
        assertThrows(AppException.class, () -> localFileService.storeFile(sourceFile, fileInfo));
    }


    @Test
    void storeFileTestFileIsNullFail() throws IOException, AppException {
        log.info("[storeFileTestFileIsNullFail]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        String fileName = UUID.randomUUID().toString() + ".c";
        FileInfo fileInfo = FileInfo.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111110")).fileStorage(fileStorage).relativePath(fileName).build();
        assertThrows(AppException.class, () -> localFileService.storeFile(null, fileInfo));
    }


    @Test
    void storeFileTestSuccess() throws IOException, AppException {
        log.info("[storeFileTestSuccess]");
        MockMultipartFile sourceFile = new MockMultipartFile("source_file",
                "test.c",
                "text/plain",
                "helloworld".getBytes());
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        String fileName = UUID.randomUUID().toString() + ".c";
        FileInfo fileInfo = FileInfo.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111110")).fileStorage(fileStorage).relativePath(fileName).build();
        localFileService.storeFile(sourceFile, fileInfo);
        Path sourceCodePath = new File(tmpdir, fileName).toPath();
        assertEquals("helloworld", new String(Files.readAllBytes(sourceCodePath)));
    }

    @Test
    void deleteFileTestFileNotFoundFail() {
        log.info("[deleteFileTestFileNotFoundFail]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        String fileName = UUID.randomUUID().toString() + ".c";
        FileInfo fileInfo = FileInfo.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111110")).fileStorage(fileStorage).relativePath(fileName).build();
        assertThrows(AppException.class, () -> localFileService.deleteFile(fileInfo));
    }

    @Test
    void deleteFileSuccess() throws AppException, IOException {
        log.info("[deleteFileSuccess]");
        String tmpdir = FileUtils.getTempDirectoryPath();
        FileStorage fileStorage = FileStorage.builder().fileStorageHost(tmpdir).fileStorageType(FileStorage.Type.VOLUME).createdBy(currentUserName).status(FileStorage.Status.ACTIVE).build();
        Path tempFilePath = Files.createTempFile("hellowold", ".c");
        String content = "#include <stdio.h>\n" +
                "int main()\n" +
                "{\n" +
                "   printf(\"Hello, World!\");\n" +
                "   return 0;\n" +
                "}";
        File file = tempFilePath.toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        FileInfo fileInfo = FileInfo.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111110")).fileStorage(fileStorage).relativePath(file.getName()).build();
        localFileService.deleteFile(fileInfo);
        assertFalse(file.exists());
    }
}
