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
import com.xcal.api.util.CommonUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileCacheService {

	@NonNull FileCacheRepository fileCacheRepository;

	public FileCache addFileCache(FileCacheDto fileCacheDto, String currentUsername) throws AppException {
		if (this.fileCacheRepository.findByCacheKey(fileCacheDto.getCacheKey()).isPresent()) {
			throw new AppException(
					AppException.LEVEL_ERROR,
					AppException.ERROR_CODE_DATA_ALREADY_EXIST,
					HttpURLConnection.HTTP_CONFLICT,
					AppException.ErrorCode.E_API_SETTING_ADD_ALREADY_EXIST.unifyErrorCode,
					CommonUtil.formatString(
							"[{}] cacheKey: {}",
							AppException.ErrorCode.E_API_SETTING_ADD_ALREADY_EXIST.messageTemplate,
							fileCacheDto.getCacheKey()
					)
			);
		}
		FileCache fileCache = FileCache.builder()
				.cacheKey(fileCacheDto.getCacheKey())
				.cacheValue(fileCacheDto.getCacheValue())
				.modifiedBy(currentUsername)
				.modifiedOn(new Date())
				.build();
		return this.fileCacheRepository.save(fileCache);
	}

	public FileCache updateFileCache(FileCacheDto fileCacheDto, String currentUsername) throws AppException {
		FileCache fileCache = this.fileCacheRepository.findByCacheKey(fileCacheDto.getCacheKey())
				.orElseThrow(() -> new AppException(
								AppException.LEVEL_ERROR,
								AppException.ERROR_CODE_DATA_NOT_FOUND,
								HttpURLConnection.HTTP_NOT_FOUND,
								AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.unifyErrorCode,
								CommonUtil.formatString(
										"[{}] cacheKey: {}",
										AppException.ErrorCode.E_API_SETTING_COMMON_NOT_FOUND.messageTemplate,
										fileCacheDto.getCacheKey()
								)
						)
				);
		fileCache.setCacheValue(fileCacheDto.getCacheValue());
		fileCache.setModifiedBy(currentUsername);
		fileCache.setModifiedOn(new Date());
		return this.fileCacheRepository.save(fileCache);
	}

	public List<FileCache> findAllFileCache() {
		return this.fileCacheRepository.findAll();
	}

}
