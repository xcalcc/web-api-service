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

import com.xcal.api.entity.FileCache;
import com.xcal.api.exception.AppException;
import com.xcal.api.model.dto.FileCacheDto;
import com.xcal.api.repository.FileCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileCacheServiceTest {

	private FileCacheRepository fileCacheRepository;

	private FileCacheService fileCacheService;

	private FileCache fileCache1;

	private FileCache fileCache2;

	@BeforeEach
	void setup() {
		this.fileCacheRepository = mock(FileCacheRepository.class);
		this.fileCacheService = new FileCacheService(this.fileCacheRepository);
		this.fileCache1 = FileCache.builder().cacheKey("test1").cacheValue("test1").build();
		this.fileCache2 = FileCache.builder().cacheKey("test2").cacheValue("test2").build();
		when(this.fileCacheRepository.findAll()).thenReturn(Arrays.asList(this.fileCache1, this.fileCache2));
	}

	@Test
	void addFileCache_CacheKeyAlreadyExists_ThrowAppException() {
		FileCacheDto fileCacheDto = FileCacheDto.builder().cacheKey("test1").cacheValue("test1").build();
		when(this.fileCacheRepository.findByCacheKey(this.fileCache1.getCacheKey())).thenReturn(Optional.of(this.fileCache1));
		assertThrows(AppException.class, () -> this.fileCacheService.addFileCache(fileCacheDto, "test"));
	}

	@Test
	void addFileCache_Success() throws AppException {
		FileCacheDto fileCacheDto = FileCacheDto.builder().cacheKey("test1").cacheValue("test1").build();
		when(this.fileCacheRepository.findByCacheKey(this.fileCache1.getCacheKey())).thenReturn(Optional.empty());
		when(this.fileCacheRepository.save(any())).thenReturn(this.fileCache1);
		FileCache fileCache = this.fileCacheService.addFileCache(fileCacheDto, "test");
		assertEquals(fileCache.getCacheKey(), this.fileCache1.getCacheKey());
		assertEquals(fileCache.getCacheValue(), this.fileCache1.getCacheValue());
	}

	@Test
	void updateFileCache_CacheKeyNotExists_ThrowAppException() {
		FileCacheDto fileCacheDto = FileCacheDto.builder().cacheKey("test1").cacheValue("test1").build();
		when(this.fileCacheRepository.findByCacheKey(this.fileCache1.getCacheKey())).thenReturn(Optional.empty());
		assertThrows(AppException.class, () -> this.fileCacheService.updateFileCache(fileCacheDto, "test"));
	}

	@Test
	void updateFileCache_Success() throws AppException {
		FileCacheDto fileCacheDto = FileCacheDto.builder().cacheKey("test1").cacheValue("test1").build();
		when(this.fileCacheRepository.findByCacheKey(this.fileCache1.getCacheKey())).thenReturn(Optional.of(this.fileCache1));
		when(this.fileCacheRepository.save(any())).thenReturn(this.fileCache1);
		FileCache fileCache = this.fileCacheService.updateFileCache(fileCacheDto, "test");
		assertEquals(fileCache.getCacheKey(), this.fileCache1.getCacheKey());
		assertEquals(fileCache.getCacheValue(), this.fileCache1.getCacheValue());
	}

	@Test
	void findAllFileCache_Success() {
		List<FileCache> fileCaches = this.fileCacheService.findAllFileCache();
		assertEquals(fileCaches.size(), 2);
		assertEquals(fileCaches.get(0).getCacheKey(), this.fileCache1.getCacheKey());
		assertEquals(fileCaches.get(0).getCacheValue(), this.fileCache1.getCacheValue());
		assertEquals(fileCaches.get(1).getCacheKey(), this.fileCache2.getCacheKey());
		assertEquals(fileCaches.get(1).getCacheValue(), this.fileCache2.getCacheValue());
	}

}
