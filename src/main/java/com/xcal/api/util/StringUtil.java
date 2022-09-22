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

package com.xcal.api.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {
    private static final String MATCH_ANY_PLACEHOLDER = "<<<match_any>>>";
    private static final String MATCH_WITHIN_FOLDER_PLACEHOLDER = "<<<match_within_folder>>>";

    private StringUtil() {
    }

    public static boolean wildCardMatch(String input, String pattern) {
        String regex = pattern.replaceAll("[^?]", "[$0]") //non ? char
                .replace("[*][*]", MATCH_ANY_PLACEHOLDER).replace("[*]", MATCH_WITHIN_FOLDER_PLACEHOLDER) //change to placeholder
                .replace(MATCH_ANY_PLACEHOLDER, ".*").replace(MATCH_WITHIN_FOLDER_PLACEHOLDER, "[^/]*") //change back to regex
                .replace("?", "."); //handle ?
        return input.matches(regex);
    }

    public static List<String> splitAndTrim( String str,String delimiter){
        if(str == null){
            return null;
        }

        return Arrays.stream(str.split(delimiter)).map(s -> s.trim()).collect(Collectors.toList());
    }

}
