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

import com.xcal.api.exception.UnexpectedPrefixException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class PathUtil {

    public static final String PATH_CATEGORY_H = "H"; //means host, under project path
    public static final String PATH_CATEGORY_T = "T"; //means target, under build path
    public static final String PATH_CATEGORY_R = "R"; //means root, under root folder

    private PathUtil() {
    }

    public static int getPathDepth(String pathStr) {
        String pathInSystem = FilenameUtils.separatorsToSystem(pathStr);
        Path path = Paths.get(pathInSystem);
        return path.getNameCount();
    }

    public static Optional<String> getParentPath(String pathStr) {
        Optional<String> parentPathStr;
        String pathInSystem = FilenameUtils.separatorsToSystem(pathStr);
        Path path = Paths.get(pathInSystem);
        Path fileName = path.getFileName();
        if (path.getParent() != null) {
            parentPathStr = Optional.of(FilenameUtils.getFullPathNoEndSeparator(StringUtils.substringBefore(pathStr, fileName.toString())));
        } else {
            parentPathStr = Optional.empty();
        }
        return parentPathStr;

    }


    public static String getPathCategory(String path) throws UnexpectedPrefixException {
        if (path.startsWith("$h/")) {
            return PATH_CATEGORY_H;
        } else if (path.startsWith("$t/")) {
            return PATH_CATEGORY_T;
        } else if (path.startsWith("/")) {
            return PATH_CATEGORY_R;
        } else {
            throw new UnexpectedPrefixException("path:" + path);
        }
    }
}
