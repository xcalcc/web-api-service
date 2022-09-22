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
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


/**
 * In version 0.8
 * - Format update
 * - Add existing issue group table
 * - Add existing issue table
 */
public class CsfReaderV08 extends CsfReader{

    public static final String supportedMagic ="XC5";
    public static final String supportedVersion ="0.8";

    private static final int FILE_PATH_TABLE_OFFSET_STARTING_BYTE = 8; //start with null
    private static final int STRING_TABLE_OFFSET_STARTING_BYTE = 16; //start with null
    private static final int ISSUE_KEY_OFFSET_STARTING_BYTE = 24; //start with null
    private static final int FIXED_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE = 32; //start without null
    private static final int NEW_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE = 40; //start without null
    private static final int P_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE = 48;
    private static final int EXISTING_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE = 56;

    private static final int ISSUE_TABLE_OFFSET_BYTE = 64 ;//Contain N and P
    private static final int EXISTING_ISSUE_TABLE_OFFSET_BYTE = 72;// Contain E

    private static final int NUM_F_ISSUE_GROUP_BYTE = 80;
    private static final int NUM_N_ISSUE_GROUP_BYTE = 84;
    private static final int NUM_L_ISSUE_GROUP_BYTE = 88;
//    private static final int TOP_COMPLEXITY_BYTE = 76;
    private static final int SCAN_ID_BYTE = 92;


    private static final int OFFSET_LEN = 8;


    //Issue Node
    private static final int ISSUE_NODE_START_COL = 4; //start from 0
    private static final int ISSUE_NODE_COL_NUM = 4;

    private static final int ISSUE_NODE_FILE_PATH_OFFSET_COL = 0;
    private static final int ISSUE_NODE_START_LINE_COL = 1;
    private static final int ISSUE_NODE_START_COLUMN_COL = 2;
    private static final int ISSUE_NODE_MESSAGE_COL = 3;

    //Issue Path, start from 0
    private static final int ISSUE_PATH_ISSUE_GROUP_UNIQUE_ID_COL = 0;
    private static final int ISSUE_PATH_ISSUE_KEY_OFFSET_COL = 1;
    private static final int ISSUE_PATH_NUM_NODE_COL = 2;
    private static final int ISSUE_PATH_CERTAINTY_COL = 3;

    //Issue group
    private static final int ISSUE_GROUP_UNIQUE_ID_COL = 0;
    private static final int ISSUE_GROUP_KEY_OFFSET_COL = 1;
    private static final int ISSUE_GROUP_RULESET_COL = 2;
    private static final int ISSUE_GROUP_RULENAME_COL = 3;
    private static final int ISSUE_GROUP_NUM_DFT_COL = 4;

    private static final int ISSUE_GROUP_SRC_FILE_OFFSET_COL = 5;
    private static final int ISSUE_GROUP_SRC_LINE_NUM_COL = 6;
    private static final int ISSUE_GROUP_SRC_COL_NUM_COL = 7;
    private static final int ISSUE_GROUP_SRC_MSG_COL = 8;

    private static final int ISSUE_GROUP_SINK_FILE_OFFSET_COL = 9;
    private static final int ISSUE_GROUP_SINK_LINE_NUM_COL = 10;
    private static final int ISSUE_GROUP_SINK_COL_NUM_COL = 11;
    private static final int ISSUE_GROUP_SINK_MSG_COL = 12;


    private static final int ISSUE_GROUP_FUNCTION_NAME_COL = 13;
    private static final int ISSUE_GROUP_VARIABLE_NAME_COL = 14;
    private static final int ISSUE_GROUP_ACC_CPIX_COL = 15;
    private static final int ISSUE_GROUP_AVG_NO_NODE_COL = 16;
    private static final int ISSUE_GROUP_SEVERITY_COL = 17;
    private static final int ISSUE_GROUP_LIKELIHOOD_COL = 18;
    private static final int ISSUE_GROUP_COST_COL = 19;
    private static final int ISSUE_GROUP_DFT_CAT_NAME_COL = 20;
    private static final int ISSUE_GROUP_CERTAINTY_COL = 21;
    private static final int ISSUE_GROUP_STATUS_COL = 22;
    private static final int ISSUE_GROUP_CRITICALITY_COL = 23;


    public static final String DILIMITER = ",";

    private static CsfReaderV08 instance = new CsfReaderV08();


    private CsfReaderV08() {
    }

    public static CsfReaderV08 getInstance() {
        return instance;
    }

    /*
    Header
     */

    public String getSupportedMagic(){
        return supportedMagic;
    }

    public String getSupportedVersion(){
        return supportedVersion;
    }

    public long getIssueTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, ISSUE_TABLE_OFFSET_BYTE);
    }

    public long getIssueTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getExistingIssueTableStartOffset(issueFile);
    }

    public long getExistingIssueTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, EXISTING_ISSUE_TABLE_OFFSET_BYTE);
    }

    public long getExistingIssueTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getFixedIssueGroupTableStartOffset(issueFile);
    }


    public long getFixedIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, FIXED_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE);
    }

    public long getFixedIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getNewIssueGroupTableStartOffset(issueFile);
    }

    public long getNewIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, NEW_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE);
    }

    public long getNewIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getPartiallyChangedIssueGroupTableStartOffset(issueFile);
    }

    public long getPartiallyChangedIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, P_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE);
    }

    public long getPartiallyChangedIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getExistingIssueGroupTableStartOffset(issueFile);

    }

    public long getExistingIssueGroupTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, EXISTING_ISSUE_GROUP_TABLE_OFFSET_STARTING_BYTE);
    }

    public long getExistingIssueGroupTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getFilePathTableStartOffset(issueFile);

    }

    /*
    Utils
     */

    public long getFilePathTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, FILE_PATH_TABLE_OFFSET_STARTING_BYTE);
    }

    public long getFilePathTableEndOffset(RandomAccessFile file) throws IOException {
        return getStringTableStartOffset(file);
    }


    public long getStringTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, STRING_TABLE_OFFSET_STARTING_BYTE);
    }

    public long getStringTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return getIssueKeyTableStartOffset(issueFile);
    }


    public long getIssueKeyTableStartOffset(RandomAccessFile issueFile) throws IOException {
        return getOffsetValue(issueFile, ISSUE_KEY_OFFSET_STARTING_BYTE);
    }

    public long getIssueKeyTableEndOffset(RandomAccessFile issueFile) throws IOException {
        return issueFile.length();
    }


    public long getOffsetValue(RandomAccessFile issueFile, long offset) throws IOException {
        byte[] bytes = new byte[OFFSET_LEN];
        for (int i = 0; i < OFFSET_LEN; i++) {
            issueFile.seek(offset + i);
            bytes[i] = issueFile.readByte();
        }

        return getLongLittleEndian(bytes);
    }



    public int getIntValue(RandomAccessFile issueFile, long offset) throws IOException {
        byte[] bytes = new byte[Integer.BYTES];
        for (int i = 0; i < Integer.BYTES; i++) {
            issueFile.seek(offset + i);
            bytes[i] = issueFile.readByte();
        }

        return getIntLittleEndian(bytes);
    }


    public int getIntLittleEndian(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public long getLongLittleEndian(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    public String[] getColsFromLine(String line) {
        return line.split(DILIMITER);
    }

    public String[] getColsWithOffset(RandomAccessFile file, String start, String offset, int radix) throws IOException {
        String line = getLineWithOffset(file, start, offset, radix);
        return line.split(DILIMITER);
    }

    public String[] getColsWithOffset(RandomAccessFile file, String offset, int radix) throws IOException {
        String line = getLineWithOffset(file, offset, radix);
        return line.split(DILIMITER);
    }

    public String[] getColsWithOffset(RandomAccessFile file, long start, long offset) throws IOException {
        String line = getLineWithOffset(file, start, offset);
        return line.split(DILIMITER);
    }

    public String[] getColsWithOffset(RandomAccessFile file, long offset) throws IOException {
        String line = getLineWithOffset(file, 0, offset);
        return line.split(DILIMITER);
    }

    public String getLineWithOffset(RandomAccessFile file, String start, String offset, int radix) throws IOException {
        return getLineWithOffset(file, Long.parseLong(start.trim(), radix), Long.parseLong(offset.trim(), radix));
    }

    public String getLineWithOffset(RandomAccessFile file, String offset, int radix) throws IOException {

        return getLineWithOffset(file, 0, Long.parseLong(offset.trim(), radix));
    }

    public String getLineWithOffset(RandomAccessFile file, long startOffset, String offset, int radix) throws IOException {

        return getLineWithOffset(file, startOffset, Long.parseLong(offset.trim(), radix));
    }


    public String getLineWithOffset(RandomAccessFile file, long start, long offset) throws IOException {
        file.seek(start + offset);
        return file.readLine();
    }

    /*
     * Issue Group
     */


    public String getIssueGroupUniqueId(String[] issueGroupLine) {
        return issueGroupLine[ISSUE_GROUP_UNIQUE_ID_COL];
    }

    public String getIssueGroupKeyOffset(String[] issueGroupLine) {
        return issueGroupLine[ISSUE_GROUP_KEY_OFFSET_COL];
    }


    public String getIssueGroupRuleName(String[] issueGroupLine) {
        return issueGroupLine[ISSUE_GROUP_RULENAME_COL];
    }

    public String getIssueGroupNumDft(String[] issueGroupLine) {
        return issueGroupLine[ISSUE_GROUP_NUM_DFT_COL];
    }

    public boolean hasSource(String[] issueLine) {
        //If one of the column is not 0, there is a source node
        return !getIssueGroupSourceFileNameOffset(issueLine).equals("0") ||
                !getIssueGroupSourceLineNumber(issueLine).equals("0") ||
                !getIssueGroupSourceColNum(issueLine).equals("0") ||
                !getIssueGroupSourceMessage(issueLine).equals("0");
    }

    public String getIssueGroupSourceFileNameOffset(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SRC_FILE_OFFSET_COL];
    }

    public String getIssueGroupSourceLineNumber(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SRC_LINE_NUM_COL];
    }

    public String getIssueGroupSourceColNum(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SRC_COL_NUM_COL];
    }

    public String getIssueGroupSourceMessage(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SRC_MSG_COL];
    }

    public boolean hasSink(String[] issueLine) {
        //If one of the column is not 0, there is a sink node
        return !getIssueGroupSinkFileNameOffset(issueLine).equals("0") ||
                !getIssueGroupSinkLineNumber(issueLine).equals("0") ||
                !getIssueGroupSinkColNum(issueLine).equals("0") ||
                !getIssueGroupSinkMessage(issueLine).equals("0");
    }

    public String getIssueGroupSinkFileNameOffset(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SINK_FILE_OFFSET_COL];
    }

    public String getIssueGroupSinkLineNumber(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SINK_LINE_NUM_COL];
    }

    public String getIssueGroupSinkColNum(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SINK_COL_NUM_COL];
    }

    public String getIssueGroupSinkMessage(String[] issueLine) {
        return issueLine[ISSUE_GROUP_SINK_MSG_COL];
    }

    public String getIssueGroupFunctionNameOffset(String[] issueLine) {
        return issueLine[ISSUE_GROUP_FUNCTION_NAME_COL];
    }

    public String getIssueGroupVariableNameOffset(String[] issueLine) {
        return issueLine[ISSUE_GROUP_VARIABLE_NAME_COL];
    }

    public String getIssueGroupAccComplexityString(String[] issueLine) {
        return issueLine[ISSUE_GROUP_ACC_CPIX_COL];
    }

    public Integer getIssueGroupCriticality(String[] issueLine) {
        return Integer.parseInt(issueLine[ISSUE_GROUP_CRITICALITY_COL]);
    }
    public String getIssueGroupCriticalityString(String[] issueLine) {
        Integer val= getIssueGroupCriticality(issueLine);
        if(val>=1 && val<=3){
            return "LOW";
        }else if(val>=4 && val<=6){
            return "MEDIUM";
        }else{//7-9
            return "HIGH";
        }
    }


    public int getIssueGroupAvgNoNode(String[] issueLine){
        return Integer.parseInt(issueLine[ISSUE_GROUP_AVG_NO_NODE_COL]);
    }


    public Integer getIssueGroupServerity(String[] issueLine) {
        return Integer.parseInt(issueLine[ISSUE_GROUP_SEVERITY_COL]);

    }

    public Integer getIssueGroupLikelihood(String[] issueLine) {
        return Integer.parseInt( issueLine[ISSUE_GROUP_LIKELIHOOD_COL]);

    }

    public Integer getIssueGroupCost(String[] issueLine) {
        return Integer.parseInt(issueLine[ISSUE_GROUP_COST_COL]);

    }

    public String getIssueGroupServerityString(String[] issueLine) {
        String severityString = issueLine[ISSUE_GROUP_SEVERITY_COL];
        if (severityString.equals("1")) {
            return "LOW";
        } else if (severityString.equals("2")) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }

    }

    public String getIssueGroupLikelihoodString(String[] issueLine) {
        String likelihood = issueLine[ISSUE_GROUP_LIKELIHOOD_COL];
        if (likelihood.equals("1")) {
            return "UNLIKELY";
        } else if (likelihood.equals("2")) {
            return "PROBABLE";
        } else {
            return "LIKELY";
        }
    }

    public String getIssueGroupCostString(String[] issueLine) {
        String cost = issueLine[ISSUE_GROUP_COST_COL];
        if (cost.equals("1")) {
            return "HIGH";
        } else if (cost.equals("2")) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    public String getIssueGroupDftCatName(String[] issueLine) {
        return issueLine[ISSUE_GROUP_DFT_CAT_NAME_COL];
    }

    public String getIssueGroupCertainty(String[] issueGroupCols) {
        return issueGroupCols[ISSUE_GROUP_CERTAINTY_COL];
    }

    public String getIssueGroupStatus(String[] issueLine){
        return issueLine[ISSUE_GROUP_STATUS_COL];
    }

    public String getIssueGroupRuleSet(String[] issueLine) {
        return issueLine[ISSUE_GROUP_RULESET_COL];
    }


    /*
     * Issue Path
     */



    public String getIssuePathIssueGroupUniqueId(String[] issueLine) {
        return issueLine[ISSUE_PATH_ISSUE_GROUP_UNIQUE_ID_COL];
    }

    public String getIssuePathIssueKeyOffset(String[] issueLine) {
        return issueLine[ISSUE_PATH_ISSUE_KEY_OFFSET_COL];
    }

    public String getIssuePathNumNode(String[] issueLine) {
        return issueLine[ISSUE_PATH_NUM_NODE_COL];
    }

    public String getIssuePathCertainty(String[] issueLine) {
        return issueLine[ISSUE_PATH_CERTAINTY_COL];
    }

    public String getIssuePathLastFilePathOffset(String[] issueLine) {
        return issueLine[issueLine.length - ISSUE_NODE_COL_NUM + ISSUE_NODE_FILE_PATH_OFFSET_COL];
    }

    public Integer getIssuePathLastLineNo(String[] issueLine) {
        return Integer.parseInt(issueLine[issueLine.length - ISSUE_NODE_COL_NUM + ISSUE_NODE_START_LINE_COL]);
    }

    public Integer getIssuePathLastColumnNo(String[] issueLine) {
        return Integer.parseInt(issueLine[issueLine.length - ISSUE_NODE_COL_NUM + ISSUE_NODE_START_COLUMN_COL]);
    }



    /*
     * Issue Node
     */


    public JSONArray getTracePathJSONArray(String[] issueLine, JSONArray jsonArray) throws FormatException {
        if ((issueLine.length - ISSUE_NODE_START_COL) % ISSUE_NODE_COL_NUM != 0) { //wrong column number
            throw new FormatException("Wrong Column number");
        }

        for (int i = ISSUE_NODE_START_COL; i < issueLine.length; i += ISSUE_NODE_COL_NUM) {
            JSONObject issueNode = new JSONObject();
            issueNode.put("fid", Integer.parseInt(issueLine[i + ISSUE_NODE_FILE_PATH_OFFSET_COL]));
            issueNode.put("ln", Integer.parseInt(issueLine[i + ISSUE_NODE_START_LINE_COL]));
            issueNode.put("cn", Integer.valueOf(issueLine[i + ISSUE_NODE_START_COLUMN_COL]));
            issueNode.put("mid", Integer.valueOf(issueLine[i + ISSUE_NODE_MESSAGE_COL]));
            jsonArray.put(issueNode);
        }
        return jsonArray;
    }

    public List<CsvTracePathDto> getTracePath(String[] issueLine) throws FormatException {
        if ((issueLine.length - ISSUE_NODE_START_COL) % ISSUE_NODE_COL_NUM != 0) { //wrong column number
            throw new FormatException("Wrong Column number");
        }
        List<CsvTracePathDto> list = new ArrayList();
        for (int i = ISSUE_NODE_START_COL; i < issueLine.length; i += ISSUE_NODE_COL_NUM) {
            CsvTracePathDto csvTracePathDto = CsvTracePathDto.builder()
                    .filePathOffset(issueLine[i + ISSUE_NODE_FILE_PATH_OFFSET_COL])
                    .startLineNo(Integer.parseInt(issueLine[i + ISSUE_NODE_START_LINE_COL]))
                    .startColumnNo(Integer.valueOf(issueLine[i + ISSUE_NODE_START_COLUMN_COL]))
                    .message(issueLine[i + ISSUE_NODE_MESSAGE_COL])
                    .build();
            list.add(csvTracePathDto);
        }
        return list;
    }





    /*
     * Other
     * */



}
