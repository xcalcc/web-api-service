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

import com.xcal.api.entity.FileInfo;
import com.xcal.api.entity.FileStorage;
import com.xcal.api.entity.ScanTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, UUID> {

    List<FileInfo> findByFileStorageAndRelativePathAndVersionAndStatusIn(FileStorage fileStorage, String relativePath, String version, List<FileInfo.Status> status);

    List<FileInfo> findByFileStorageAndRelativePathStartsWithAndStatusIn(FileStorage fileStorage, String relativePath, List<FileInfo.Status> status);

    List<FileInfo> findByFileStorageAndStatusIn(FileStorage fileStorage, List<FileInfo.Status> status);

    List<FileInfo> findByFileStorageAndVersionAndStatusIn(FileStorage fileStorage, String version, List<FileInfo.Status> status);

    List<FileInfo> findByScanFilesScanTask(ScanTask scanTask);

    List<FileInfo> findByFileStorageAndChecksumAndStatusIn(FileStorage fileStorage, String checksum, List<FileInfo.Status> status);

    List<FileInfo> findByTypeAndCreatedOnBefore(FileInfo.Type type, Date createOn);

    Page<FileInfo> findByNameAndTypeAndVersionAndStatusIn(String name, FileInfo.Type type, String version, List<FileInfo.Status> status, Pageable pageable);

    @Modifying
    @Query(value = "delete from file_info fi " +
            "where fi.id in " +
            "(select sf.file_info_id from scan_file sf  " +
            "inner join scan_task st on st.id=sf.scan_task_id " +
            "where st.id=?1)", nativeQuery = true)
    void deleteByScanTask(UUID scanTaskId);
}
