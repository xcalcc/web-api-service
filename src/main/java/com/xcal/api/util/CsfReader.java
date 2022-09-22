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

import com.xcal.api.exception.FormatException;
import com.xcal.api.model.dto.CsvTracePathDto;
import org.json.JSONArray;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public abstract class CsfReader {

    public static String FILE_EXTENSION = ".csf";

    /*
     * Header
     */


    abstract public String getSupportedMagic();

    public static String getMagic(RandomAccessFile issueFile) throws IOException {
        return getOffsetString(issueFile, 0, 4).replace(",", "");
    }

    abstract public String getSupportedVersion();

    public static String getVersion(RandomAccessFile issueFile) throws IOException {
        return getOffsetString(issueFile, 4, 4).replace(",", "");
    }

    abstract public long getIssueTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getIssueTableEndOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getExistingIssueTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getExistingIssueTableEndOffset(RandomAccessFile issueFile) throws IOException;


    abstract public long getFixedIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getFixedIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getNewIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getNewIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getPartiallyChangedIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getPartiallyChangedIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getExistingIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getExistingIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getFilePathTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getFilePathTableEndOffset(RandomAccessFile file) throws IOException;


    abstract public long getStringTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getStringTableEndOffset(RandomAccessFile issueFile) throws IOException;


    abstract public long getIssueKeyTableStartOffset(RandomAccessFile issueFile) throws IOException;

    abstract public long getIssueKeyTableEndOffset(RandomAccessFile issueFile) throws IOException;

    /*
     * Util
     */

    public static String getOffsetString(RandomAccessFile issueFile, long offset, int length) throws IOException {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            issueFile.seek(offset + i);
            bytes[i] = issueFile.readByte();
        }

        return new String(bytes);
    }


    abstract public long getOffsetValue(RandomAccessFile issueFile, long offset) throws IOException;


    abstract public int getIntValue(RandomAccessFile issueFile, long offset) throws IOException;

    abstract public int getIntLittleEndian(byte[] bytes);

    abstract public long getLongLittleEndian(byte[] bytes);

    abstract public String[] getColsFromLine(String line);

    abstract public String[] getColsWithOffset(RandomAccessFile file, String start, String offset, int radix) throws IOException;


    abstract public String[] getColsWithOffset(RandomAccessFile file, String offset, int radix) throws IOException;

    abstract public String[] getColsWithOffset(RandomAccessFile file, long start, long offset) throws IOException;

    abstract public String[] getColsWithOffset(RandomAccessFile file, long offset) throws IOException;

    abstract public String getLineWithOffset(RandomAccessFile file, String start, String offset, int radix) throws IOException;

    abstract public String getLineWithOffset(RandomAccessFile file, String offset, int radix) throws IOException;

    abstract public String getLineWithOffset(RandomAccessFile file, long startOffset, String offset, int radix) throws IOException;


    abstract public String getLineWithOffset(RandomAccessFile file, long start, long offset) throws IOException;

    /*
     * Issue Group
     */


    abstract public String getIssueGroupUniqueId(String[] issueGroupLine);

    abstract public String getIssueGroupKeyOffset(String[] issueGroupLine);

    abstract public String getIssueGroupRuleName(String[] issueGroupLine);

    abstract public String getIssueGroupNumDft(String[] issueGroupLine);

    abstract public boolean hasSource(String[] issueLine);

    abstract public String getIssueGroupSourceFileNameOffset(String[] issueLine);

    abstract public String getIssueGroupSourceLineNumber(String[] issueLine);

    abstract public String getIssueGroupSourceColNum(String[] issueLine);

    abstract public String getIssueGroupSourceMessage(String[] issueLine);

    abstract public boolean hasSink(String[] issueLine);

    abstract public String getIssueGroupSinkFileNameOffset(String[] issueLine);

    abstract public String getIssueGroupSinkLineNumber(String[] issueLine);

    abstract public String getIssueGroupSinkColNum(String[] issueLine);

    abstract public String getIssueGroupSinkMessage(String[] issueLine);

    abstract public String getIssueGroupFunctionNameOffset(String[] issueLine);

    abstract public String getIssueGroupVariableNameOffset(String[] issueLine);

    abstract public String getIssueGroupAccComplexityString(String[] issueLine);

    abstract public Integer getIssueGroupCriticality(String[] issueLine);

    abstract public String getIssueGroupCriticalityString(String[] issueLine);


    abstract public int getIssueGroupAvgNoNode(String[] issueLine);


    abstract public Integer getIssueGroupServerity(String[] issueLine);

    abstract public Integer getIssueGroupLikelihood(String[] issueLine);

    abstract public Integer getIssueGroupCost(String[] issueLine);

    abstract public String getIssueGroupServerityString(String[] issueLine);

    abstract public String getIssueGroupLikelihoodString(String[] issueLine);

    abstract public String getIssueGroupCostString(String[] issueLine);

    abstract public String getIssueGroupDftCatName(String[] issueLine);

    abstract public String getIssueGroupCertainty(String[] issueGroupCols);

    abstract public String getIssueGroupStatus(String[] issueLine);

    abstract public String getIssueGroupRuleSet(String[] issueLine);

    /*
     * Issue Path
     */


    abstract public String getIssuePathIssueGroupUniqueId(String[] issueLine);

    abstract public String getIssuePathIssueKeyOffset(String[] issueLine);

    abstract public String getIssuePathNumNode(String[] issueLine);

    abstract public String getIssuePathCertainty(String[] issueLine);

    abstract public String getIssuePathLastFilePathOffset(String[] issueLine);

    abstract public Integer getIssuePathLastLineNo(String[] issueLine);

    abstract public Integer getIssuePathLastColumnNo(String[] issueLine);


    /*
     * Issue Node
     */


    abstract public JSONArray getTracePathJSONArray(String[] issueLine, JSONArray jsonArray) throws FormatException;

    abstract List<CsvTracePathDto> getTracePath(String[] issueLine) throws FormatException;


    /*
     * Other
     */

    public static CsfReader getInstance(String version) {
        if (version.equals(CsfReaderV08.supportedVersion)) {
            return CsfReaderV08.getInstance();
        } else if (version.equals(CsfReaderV081.supportedVersion)) {
            return CsfReaderV081.getInstance();
        } else {//Default
            return CsfReaderV081.getInstance();
        }
    }
}
