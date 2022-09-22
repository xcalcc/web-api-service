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

import com.xcal.api.entity.AsyncJob;
import com.xcal.api.repository.AsyncJobRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AsyncJobService {

	@NonNull AsyncJobRepository asyncJobRepository;

	public AsyncJob addAsyncJob(AsyncJob asyncJob) {
		asyncJob = this.asyncJobRepository.save(asyncJob);
		this.asyncJobRepository.flush();
		return asyncJob;
	}

	public AsyncJob updateAsyncJob(AsyncJob asyncJob) {
		asyncJob = this.asyncJobRepository.save(asyncJob);
		this.asyncJobRepository.flush();
		return asyncJob;
	}

	public Optional<AsyncJob> findAsyncJobById(UUID id) {
		return this.asyncJobRepository.findById(id);
	}

}
