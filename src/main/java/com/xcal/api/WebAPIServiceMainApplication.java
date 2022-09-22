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

package com.xcal.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootApplication
@EnableSwagger2
@EnableAsync
@EnableCaching
@EnableScheduling
public class WebAPIServiceMainApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(WebAPIServiceMainApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		List<String> profiles = new ArrayList<>();

		List<String> modes = new ArrayList<>();
        if (Arrays.stream(args).anyMatch(arg -> StringUtils.startsWith(arg, "--mode="))) {
            String modesString = Arrays.stream(args).filter(arg -> StringUtils.startsWith(arg, "--mode=")).findFirst().orElse("");
            modesString = StringUtils.substringAfter(modesString, "--mode=");
			if(StringUtils.isNotBlank(modesString)){
				modes.addAll(Arrays.asList(StringUtils.split(modesString, ",")));
			}
            if(log.isInfoEnabled()){
				log.info("[main] Application mode: {}", String.join(",", modes));
			}
		}

        app.setAdditionalProfiles(profiles.toArray(new String[0]));
        if(log.isInfoEnabled()){
			log.info("[main] args: {}", StringUtils.join(args,", "));
		}
        app.run(args);
		log.info("[main] Application Completed");
	}
}
