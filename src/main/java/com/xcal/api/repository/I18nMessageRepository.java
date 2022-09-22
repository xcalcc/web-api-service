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

import com.xcal.api.entity.I18nMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface I18nMessageRepository extends JpaRepository<I18nMessage, UUID> {

    List<I18nMessage> findByKey(String key);

    List<I18nMessage> findByKeyIn(List<String> keys);

    List<I18nMessage> findByKeyAndLocale(String key, String locale);

    List<I18nMessage> findByKeyInAndLocale(List<String> keys, String locale);

    List<I18nMessage> findByKeyStartsWith(String key);

    List<I18nMessage> findByKeyStartsWithAndLocale(String prefix, String locale);
}
