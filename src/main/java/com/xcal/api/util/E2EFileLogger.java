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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class E2EFileLogger implements Closeable {

    private FileWriter fw;
    private BufferedWriter bw;

    String strDateFormat = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    String scanTaskId;

    public E2EFileLogger(FileWriter fw, BufferedWriter bw, String scanTaskId) {
            this.fw = fw;
            this.bw = bw;
            this.scanTaskId = scanTaskId;
    }

    public E2EFileLogger(String filePath, String scanTaskId, boolean append) {
        try {
            FileUtil.createFileIfNotExist(filePath);
            fw = new FileWriter(filePath, append);
            bw = new BufferedWriter(fw);
            this.scanTaskId = scanTaskId;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine(String value) {
        if (bw == null || value == null) {
            return;
        }
        try {
            bw.write(sdf.format(new Date()) + "," + value);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String value) {
        if (bw == null || value == null) {
            return;
        }
        try {
            bw.write(sdf.format(new Date()) + " - " + scanTaskId + " " + value);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newLine() {
        if (bw == null) {
            return;
        }
        try {
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (bw == null || fw == null) {
            return;
        }
        try {
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
