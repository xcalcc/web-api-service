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

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.thymeleaf.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class RepositoryCustom {

    static List<Order> toOrders(CriteriaBuilder cb, Root root, Sort sort){
        log.debug("[toOrders] root: {}, sort: {}", root.getModel().getName(), sort);
        return sort.stream().map(o -> {
            Order order;
            if(o.isAscending()){
                order = cb.asc(getPath(root, o.getProperty()));
            }else{
                order = cb.desc(getPath(root, o.getProperty()));
            }
            return order;
        }).collect(Collectors.toList());
    }

    static List<Order> toOrders(CriteriaBuilder cb, Path path, Sort sort){
        log.debug("[toOrders] path: {}, sort: {}", path.toString(), sort);
        return sort.stream().map(o -> {
            Order order;
            if(o.isAscending()){
                order = cb.asc(getPath(path, o.getProperty()));
            }else{
                order = cb.desc(getPath(path, o.getProperty()));
            }
            return order;
        }).collect(Collectors.toList());
    }

    private static Path getPath(Path path, String attribute) {
        Path result;
        if(StringUtils.contains(attribute, ".")){
            String attr = StringUtils.substringBefore(attribute, ".");
            String rest = StringUtils.substringAfter(attribute,".");
            result = getPath(path.get(attr), rest);
        }else{
            result = path.get(attribute);
        }
        return result;
    }
}
