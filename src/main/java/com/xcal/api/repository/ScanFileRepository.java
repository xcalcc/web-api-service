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

package com.xcal.api.repository;

import com.xcal.api.entity.ScanFile;
import com.xcal.api.entity.ScanTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ScanFileRepository extends JpaRepository<ScanFile, UUID>, ScanFileRepositoryCustom {

	Optional<ScanFile> findByScanTaskAndProjectRelativePath(ScanTask scanTask, String relativePath);

	List<ScanFile> findByScanTaskAndStorePathStartsWith(ScanTask scanTask, String storePath);

	List<ScanFile> findByScanTaskAndType(ScanTask scanTask, ScanFile.Type type);

	List<ScanFile> findByScanTaskAndProjectRelativePathIn(ScanTask scanTask, Set<String> projectRelativePath);

	@Modifying
	@Query(value = "delete from scan_file sf where sf.scan_task_id = ?1", nativeQuery = true)
	void deleteByScanTask(UUID scanTaskId);
}
