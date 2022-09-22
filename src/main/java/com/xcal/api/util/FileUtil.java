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

import org.apache.any23.encoding.TikaEncodingDetector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class FileUtil {
    private FileUtil(){}
    public static Charset detectCharset(InputStream is) throws IOException {
        return Charset.forName(new TikaEncodingDetector().guessEncoding(is));
    }

    public static boolean createFileIfNotExist(String filePath) throws IOException {
        File scanTasklogFile = new File(filePath);
        return scanTasklogFile.createNewFile();
    }
}