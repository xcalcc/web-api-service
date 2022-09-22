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

package com.xcal.api.controller.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xcal.api.service.HouseKeepService;
import io.opentracing.Tracer;
import io.swagger.annotations.Api;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/house_keep_service/v3")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "HouseKeep Service")
public class HouseKeepController {

    @NonNull HouseKeepService houseKeepService;
    @NonNull Tracer tracer;

    @NonNull ObjectMapper om;


}
