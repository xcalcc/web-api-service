package com.xcal.api.util;


import com.xcal.api.exception.FormatException;
import com.xcal.api.model.dto.CsvTracePathDto;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@AutoConfigureMockMvc
public class CsfReader08Test {

    @MockBean
    RandomAccessFile mockIssueFile;
    static RandomAccessFile issueFile;
    private String issueGroupString = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,1,1,2,Vul,D,N,43962";
    private String issueLineString = "abc00001,2,1,D,2,61,0,13";
    private long expectedStringTableStart = 2603;
    private long expectedFilePathTableStart = 395;
    public static final int issueKeyTableEndOffset = 2727;
    String firstNewIssueOffset = "96";

    @BeforeAll
    public static void init() throws IOException {
        String issueFileName = "src/test/tmemory_leak.csf";
        issueFile = new RandomAccessFile(new File(issueFileName), "r");

        FileChannel fileChannel = issueFile.getChannel();

        //Get direct byte buffer access using channel.map() operation
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

        //You can read the file from this buffer the way you like.
        int lineNum = 1;
        int colNum = 1;

        for (int i = 0; i < buffer.limit(); i++) {

            byte currentByte = buffer.get();
            System.out.print((char) currentByte); //Print the content of file
            if (((char) currentByte) == ',') {
                colNum++;
                System.out.println(buffer.position());
            }
            if (((char) currentByte) == '\n') {
                colNum = 1;
                lineNum++;
                System.out.println(buffer.position());
            }
        }
    }

    @AfterAll
    public static void cleanup() throws IOException {
        issueFile.close();
    }


    @Test
    public void testGetInstance_normal_expectSuccess() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        assertNotNull(csfReader);

    }

    @Test
    public void getFixedIssueGroupTableEndOffset_normalCase_expectSuccess() throws IOException {

        CsfReader csfReader = CsfReaderV08.getInstance();

        //get string table
        long issueTableEnd = csfReader.getFixedIssueGroupTableEndOffset(issueFile);
        long fileTableStart = csfReader.getNewIssueGroupTableStartOffset(issueFile);
        assertEquals(fileTableStart, issueTableEnd);

    }

    @Test
    public void testGetNewIssueTableStart_normalCase_expectSuccess() throws IOException {


        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        CsfReaderV08 spyCsfReader=Mockito.spy(csfReader);

        doReturn(10L).when(spyCsfReader).getOffsetValue(any(RandomAccessFile.class),anyLong());
        long issueTableEnd = spyCsfReader.getIssueTableStartOffset(issueFile);

        assertEquals(10L, issueTableEnd);

    }

    @Test
    public void getIssueGroupTableEndOffset_normalCase_expectSuccess() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        CsfReaderV08 spyCsfReader=Mockito.spy(csfReader);

        doReturn(10L).when(spyCsfReader).getExistingIssueTableStartOffset(any(RandomAccessFile.class));
        long issueTableEnd = spyCsfReader.getIssueTableEndOffset(issueFile);

        assertEquals(10L, issueTableEnd);

    }

    @Test
    public void testGetFileTableStart_normalCase_expectSuccess() throws IOException {


        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        //get string table
        long fileTableStart = csfReader.getFilePathTableStartOffset(issueFile);
        System.out.println(csfReader.getLineWithOffset(issueFile, 0, fileTableStart));
        assertEquals(expectedFilePathTableStart, fileTableStart);

    }

    @Test
    public void testGetFileTableEnd_normalCase_expectSuccess() throws IOException {


        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        //get string table
        long fileTableEnd = csfReader.getFilePathTableEndOffset(issueFile);
        long stringTableStart = csfReader.getStringTableStartOffset(issueFile);

        assertEquals(stringTableStart, fileTableEnd);

    }

    @Test
    public void testGetStringTableStart_normalCase_expectSuccess() throws IOException {


        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        //get string table
        long stringTableStart = csfReader.getStringTableStartOffset(issueFile);
        assertEquals(2603, stringTableStart);

    }


//    @Test
//    public void testgetStringTableStart_negativeValue_expect() throws IOException {
//
//        fail();
//
//    }
//
//    @Test
//    public void testgetStringTableStart_valueLargerThanFile_expect() {
//        fail();
//    }

    @Test
    public void testgetIssue_normal_expectSuccess() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueLine = csfReader.getLineWithOffset(issueFile, firstNewIssueOffset, 10);
        assertEquals(issueLineString, issueLine);
    }


//    @Test
//    public void testgetIssue_columeNotEnough_expect() {
//        fail();
//    }

//    @Test
//    public void testgetIssue_largetThanFile_expect() throws IOException {
//        String issueOffset = "30000";
//        CsvDotVReader csvDotVReader = CsvDotVReader.getInstance();
//
//        String issueLine = csvDotVReader.getLineWithOffset(issueFile, issueOffset, 10);
//
//        //don't have checking
//        fail();
//    }

    @Test
    public void testgetIssue_negativeOffset_expectIOException() throws IOException {
        Assertions.assertThrows(IOException.class, () -> {
            String issueOffset = "-3000";
            CsfReaderV08 csfReader = CsfReaderV08.getInstance();

            String issueLine = csfReader.getLineWithOffset(issueFile, issueOffset, 10);
        });
    }


    @Test
    public void testgetIssueCols_2argNormal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] cols = csfReader.getColsWithOffset(issueFile, 96);
        String[] expectedCols = issueLineString.split(",");
        assertEquals(expectedCols.length, cols.length);
        for (int i = 0; i < expectedCols.length; i++) {
            assertEquals(expectedCols[i], cols[i]);
        }

    }

    @Test
    public void testgetIssueCols_4argNormal_expectSuccess() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] cols = csfReader.getColsWithOffset(issueFile, "0", firstNewIssueOffset, 10);
        String[] expectedCols = issueLineString.split(",");
        assertEquals(expectedCols.length, cols.length);
        for (int i = 0; i < expectedCols.length; i++) {
            assertEquals(expectedCols[i], cols[i]);
        }

    }

    @Test
    public void testgetIssueCols_normal_expectSuccess() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] cols = csfReader.getColsWithOffset(issueFile, firstNewIssueOffset, 10);
        String[] expectedCols = issueLineString.split(",");
        assertEquals(expectedCols.length, cols.length);
        for (int i = 0; i < expectedCols.length; i++) {
            assertEquals(expectedCols[i], cols[i]);
        }

    }


    @Test
    public void testgetIssueCols_offsetLargerThanFile_expect() throws IOException {
        Assertions.assertThrows(IOException.class, () -> {
            String issueOffset = "-227";
            CsfReaderV08 csfReader = CsfReaderV08.getInstance();
            String[] cols = csfReader.getColsWithOffset(issueFile, issueOffset, 10);
        });
    }

    @Test
    public void getIssueFilePathOffset_normal_lastTracePathFilePath() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueLineString.split(",");
        String value = csfReader.getIssuePathLastFilePathOffset(issueLine);
        assertEquals("2", value);
    }

    @Test
    public void getIssueLineNo_normal_lastTracePathLineNumber() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueLineString.split(",");
        long value = csfReader.getIssuePathLastLineNo(issueLine);
        assertEquals(61, value);
    }

    @Test
    public void getIssueColumnNo_normal_lastTracePathLColumnNumber() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueLineString.split(",");
        long value = csfReader.getIssuePathLastColumnNo(issueLine);
        assertEquals(0, value);
    }

    @Test
    public void getIssueKeyTableEndOffset_normal_correctLength() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        long value = csfReader.getIssueKeyTableEndOffset(issueFile);
        assertEquals(issueKeyTableEndOffset, value);
    }

//    @Test
//    public void getIssueRuleCode_col4_correctValue() throws IOException {
//        CsvDotVReader csvDotVReader = CsvDotVReader.getInstance();
//        String[] issueLine = new String[30];
//        int col=4;
//        issueLine[col]="AOB";
//        String value = csvDotVReader.getIssueRuleCode(issueLine);
//        assertEquals("AOB", value);
//    }


    @Test
    public void getIssueGroupRuleName_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupRuleName(issueLine);
        assertEquals("NPD0", complexity);
    }


    @Test
    public void getIssueGroupNumDft_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupNumDft(issueLine);
        assertEquals("2", complexity);
    }

    @Test
    public void getIssueGroupAccComplexity_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupAccComplexityString(issueLine);
        assertEquals("5678", complexity);
    }


    @Test
    public void getIssueGroupCriticality_greaterThan9_high() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,12,0,1,1,2,Vul,D,N,43962,12".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupCriticality_9_high() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,9,0,1,1,2,Vul,D,N,43962,9".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupCriticality_8_high() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,8,0,1,1,2,Vul,D,N,43962,8".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupCriticality_7_high() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,7,0,1,1,2,Vul,D,N,43962,7".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupCriticality_6_medium() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,6,0,1,1,2,Vul,D,N,6".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("MEDIUM", complexity);
    }

    @Test
    public void getIssueGroupCriticality_5_medium() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5,0,1,1,2,Vul,D,N,5".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("MEDIUM", complexity);
    }

    @Test
    public void getIssueGroupCriticality_4_medium() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,4,0,1,1,2,Vul,D,N,4".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("MEDIUM", complexity);
    }

    @Test
    public void getIssueGroupCriticality_3_low() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,3,0,1,1,2,Vul,D,N,3".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("LOW", complexity);
    }

    @Test
    public void getIssueGroupCriticality_2_low() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,2,0,1,1,2,Vul,D,N,2".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("LOW", complexity);
    }

    @Test
    public void getIssueGroupCriticality_1_low() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,1,0,1,1,2,Vul,D,N,1".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("LOW", complexity);
    }

    @Test
    public void getIssueGroupCriticality_lessThan1_HIGH() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = "abc00001,2,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,-1,0,1,1,2,Vul,D,N,-1".split(",");
        String complexity = csfReader.getIssueGroupCriticalityString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupServerity_normal_1() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,1,1,2,Vul";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupServerity(issueLine);
        assertEquals(1, value);
    }

    @Test
    public void getIssueGroupServerity_normal_2() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,2,1,2,Vul";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupServerity(issueLine);
        assertEquals(2, value);
    }

    @Test
    public void getIssueGroupServerity_normal_3() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,1,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupServerity(issueLine);
        assertEquals(3, value);
    }

    @Test
    public void getIssueGroupServerity_normal_low() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupServerityString(issueLine);
        assertEquals("LOW", complexity);
    }

    @Test
    public void getIssueGroupServerity_normal_medium() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,2,1,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupServerityString(issueLine);
        assertEquals("MEDIUM", complexity);
    }

    @Test
    public void getIssueGroupServerity_normal_high() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,1,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupServerityString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupLikelihood_normal_1() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,1,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupLikelihood(issueLine);
        assertEquals(1, value);
    }

    @Test
    public void getIssueGroupLikelihood_normal_2() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,2,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupLikelihood(issueLine);
        assertEquals(2, value);
    }

    @Test
    public void getIssueGroupLikelihood_normal_3() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupLikelihood(issueLine);
        assertEquals(3, value);
    }

    @Test
    public void getIssueGroupLikelihood_normal_unlikely() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,1,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupLikelihoodString(issueLine);
        assertEquals("UNLIKELY", complexity);
    }

    @Test
    public void getIssueGroupLikelihood_normal_probable() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,2,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupLikelihoodString(issueLine);
        assertEquals("PROBABLE", complexity);
    }

    @Test
    public void getIssueGroupLikelihood_normal_likely() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupLikelihoodString(issueLine);
        assertEquals("LIKELY", complexity);
    }

    @Test
    public void getIssueGroupCost_normal_3() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,3,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupCost(issueLine);
        assertEquals(3, value);
    }

    @Test
    public void getIssueGroupCost_normal_2() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupCost(issueLine);
        assertEquals(2, value);
    }

    @Test
    public void getIssueGroupCost_normal_1() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,1,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        Integer value = csfReader.getIssueGroupCost(issueLine);
        assertEquals(1, value);
    }

    @Test
    public void getIssueGroupCost_normal_high() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,1,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupCostString(issueLine);
        assertEquals("HIGH", complexity);
    }

    @Test
    public void getIssueGroupCost_normal_medium() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,2,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupCostString(issueLine);
        assertEquals("MEDIUM", complexity);
    }

    @Test
    public void getIssueGroupCost_normal_low() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,3,3,3,Vul,43962";
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupCostString(issueLine);
        assertEquals("LOW", complexity);
    }

    @Test
    public void getIssueGroupDftCatName_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupDftCatName(issueLine);
        assertEquals("Vul", complexity);
    }

    @Test
    public void getIssueGroupCertainty_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String value = csfReader.getIssueGroupCertainty(issueLine);
        assertEquals("D", value);
    }


    @Test
    public void getIssueGroupStatus_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String complexity = csfReader.getIssueGroupStatus(issueLine);
        assertEquals("N", complexity);
    }

    @Test
    public void getIssueGroupRuleSet_normal_Default() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String value = csfReader.getIssueGroupRuleSet(issueLine);
        assertEquals("X", value);
    }

    @Test
    public void testgetIssueFilePathOffset_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String pathOffset = csfReader.getIssueGroupSourceFileNameOffset(issueLine);
        assertEquals("0", pathOffset);
    }

    @Test
    public void getIssueGroupFunctionNameOffset_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String pathOffset = csfReader.getIssueGroupFunctionNameOffset(issueLine);
        System.out.println(pathOffset);
        assertEquals("27", pathOffset);
    }

    @Test
    public void getIssueGroupVariableNameOffset_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueGroupString.split(",");
        String pathOffset = csfReader.getIssueGroupVariableNameOffset(issueLine);
        System.out.println(pathOffset);
        assertEquals("19", pathOffset);
    }


    @Test
    public void getIssueGroupUniqueId_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        String[] issueLine = issueGroupString.split(",");
        String issueKeyOffset = csfReader.getIssueGroupUniqueId(issueLine);
        assertEquals("abc00001", issueKeyOffset);

    }

    @Test
    public void testgetIssueKeyOffset_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        String[] issueLine = issueGroupString.split(",");
        String issueKeyOffset = csfReader.getIssueGroupKeyOffset(issueLine);
        assertEquals("2", issueKeyOffset);

    }

    @Test
    public void getIssuePathIssueGroupUniqueId_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        String[] issueLine = issueLineString.split(",");
        String issueKeyOffset = csfReader.getIssuePathIssueGroupUniqueId(issueLine);
        assertEquals("abc00001", issueKeyOffset);

    }

    @Test
    public void getIssuePathStringId_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        String[] issueLine = issueLineString.split(",");
        String issueKeyOffset = csfReader.getIssuePathIssueKeyOffset(issueLine);
        assertEquals("2", issueKeyOffset);

    }

    @Test
    public void getIssuePathNumNode_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        String[] issueLine = issueLineString.split(",");
        String issueKeyOffset = csfReader.getIssuePathNumNode(issueLine);
        assertEquals("1", issueKeyOffset);

    }

    @Test
    public void getIssuePathCertainty_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();

        String[] issueLine = issueLineString.split(",");
        String issueKeyOffset = csfReader.getIssuePathCertainty(issueLine);
        assertEquals("D", issueKeyOffset);

    }

//    @Test
//    public void getIssuePathNumIssueSameGroup_normal_expectSuccess() throws IOException {
//        CsvDotVReader csvDotVReader = CsvDotVReader.getInstance();
//
//        String[] issueLine = issueLineString.split(",");
//        String issueKeyOffset = csvDotVReader.getIssuePathNumIssueSameGroup(issueLine);
//        assertEquals("68", issueKeyOffset);
//
//    }

    @Test
    public void testGetValueFromPathTable_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String value = csfReader.getLineWithOffset(issueFile, expectedFilePathTableStart, 2);
        assertEquals("/home/sunchan/v2csv/test/../../test/tmemory_leak.cxx", value);
    }

    @Test
    public void getValueFromStringTable_normal_expectSuccess() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String value = csfReader.getLineWithOffset(issueFile, expectedStringTableStart, 2);
        assertEquals("tmemory_leak.cxx", value);
    }

//    @Test
//    public void testGetValueFromStringTable_offsetNotFound_expect() {
//        fail();
//    }

//    @Test
//    public void testGetValueFromStringTable_largetThanFile_expect() {
//        fail();
//    }

    @Test
    public void testGetValueFromStringTable_negativeOffset_expect() throws IOException {
        Assertions.assertThrows(IOException.class, () -> {
            CsfReaderV08 csfReader = CsfReaderV08.getInstance();
            String value = csfReader.getLineWithOffset(issueFile, -1791, 43);

        });
    }


    @Test
    public void testgetStringTableEndOffset_normal_expectLastChar() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        long offset = csfReader.getStringTableEndOffset(issueFile);
        long issueKeyStartOffset = csfReader.getIssueKeyTableStartOffset(issueFile);
        assertEquals(issueKeyStartOffset, offset);

    }


    @Test
    public void getColsFromLine_normal_correctValue() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = csfReader.getColsFromLine(issueLineString);
        assertEquals(8,issueLine.length);
        assertEquals("abc00001",issueLine[0]);
        assertEquals("2",issueLine[1]);
        assertEquals("1",issueLine[2]);
        assertEquals("D",issueLine[3]);
        assertEquals("2",issueLine[4]);
        assertEquals("61",issueLine[5]);
        assertEquals("0",issueLine[6]);
        assertEquals("13",issueLine[7]);

    }

    @Test
    public void testgetTracePath_normal_expectSuccess() throws IOException, FormatException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueLineString.split(",");
        List<CsvTracePathDto> csvTracePathDtoList = csfReader.getTracePath(issueLine);
        CsvTracePathDto csvTracePathDto;
        csvTracePathDto = csvTracePathDtoList.get(0);
        System.out.println(csvTracePathDto);
        assertEquals("2", csvTracePathDto.getFilePathOffset());
        assertEquals(61, csvTracePathDto.getStartLineNo());
        assertEquals(0, csvTracePathDto.getStartColumnNo());
        assertEquals("13", csvTracePathDto.getMessage());


    }

    @Test
    public void hasSource_all0_noSource() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,0,2,31,0,3,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSource(issueGroupCols);
        assertEquals(false, value);

    }

    @Test
    public void hasSource_fileNot0_hasSource() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,1,0,0,0,2,31,0,3,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSource(issueGroupCols);
        assertEquals(true, value);

    }

    @Test
    public void hasSource_lineNot0_hasSource() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,1,0,0,2,31,0,3,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSource(issueGroupCols);
        assertEquals(true, value);

    }

    @Test
    public void hasSource_columnNot0_hasSource() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,1,0,2,31,0,3,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSource(issueGroupCols);
        assertEquals(true, value);

    }

    @Test
    public void hasSource_messageNot0_hasSource() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,0,0,0,1,2,31,0,3,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSource(issueGroupCols);
        assertEquals(true, value);

    }


    @Test
    public void hasSource_all0_noSink() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,2,31,0,3,0,0,0,0,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSink(issueGroupCols);
        assertEquals(false, value);

    }

    @Test
    public void hasSink_fileNot0_hasSink() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,2,31,0,3,1,0,0,0,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSink(issueGroupCols);
        assertEquals(true, value);

    }

    @Test
    public void hasSink_lineNot0_hasSink() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,2,31,0,3,0,1,0,0,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSink(issueGroupCols);
        assertEquals(true, value);

    }

    @Test
    public void hasSink_columnNot0_hasSink() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,2,31,0,3,0,0,1,0,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSink(issueGroupCols);
        assertEquals(true, value);

    }

    @Test
    public void hasSink_messageNot0_hasSink() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String issueGroupString = "2,12345,X,NPD0,2,2,31,0,3,0,0,0,1,27,19,5678,0,1,1,2,Vul,43962";
        String[] issueGroupCols = issueGroupString.split(",");
        boolean value = csfReader.hasSink(issueGroupCols);
        assertEquals(true, value);

    }


    @Test
    public void testgetTracePath_wrongColumnNumber_expectRuntimeException() throws IOException {
        Assertions.assertThrows(FormatException.class, () -> {
            String issueRowWithWrongColNum = "2,5,68,2,5,0,1,2,7,0,24,2,7,0,26,2,7,0,17,2,11,0";
            CsfReaderV08 csfReader = CsfReaderV08.getInstance();
            String[] issueLine = issueRowWithWrongColNum.split(",");
            List<CsvTracePathDto> csvTracePathDtoList = csfReader.getTracePath(issueLine);
            CsvTracePathDto csvTracePathDto;
            csvTracePathDto = csvTracePathDtoList.get(0);
            System.out.println(csvTracePathDto);
            assertEquals("510", csvTracePathDto.getFilePathOffset());
            assertEquals(564, csvTracePathDto.getStartLineNo());
            assertEquals(0, csvTracePathDto.getStartColumnNo());
            assertEquals("21", csvTracePathDto.getMessage());
        });
    }


    @Test
    public void getTracePathJSONArray_normal_0nodes() throws IOException, FormatException {

        String issueRowWithWrongColNum = "abc00001,2,0,D,";
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueRowWithWrongColNum.split(",");
        JSONArray jsonArray = new JSONArray();
        jsonArray = csfReader.getTracePathJSONArray(issueLine, jsonArray);
        assertEquals(0, jsonArray.length());

    }

    @Test
    public void getTracePathJSONArray_normal_1nodes() throws IOException, FormatException {

        String issueRowWithWrongColNum = "abc00001,2,1,D,2,5,0,1";
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueRowWithWrongColNum.split(",");
        JSONArray jsonArray = new JSONArray();
        jsonArray = csfReader.getTracePathJSONArray(issueLine, jsonArray);
        assertEquals(1, jsonArray.length());

    }

    @Test
    public void getTracePathJSONArray_normal_2nodes() throws IOException, FormatException {

        String issueRowWithWrongColNum = "abc00001,2,2,D,2,5,0,1,2,7,0,24";
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        String[] issueLine = issueRowWithWrongColNum.split(",");
        JSONArray jsonArray = new JSONArray();
        jsonArray = csfReader.getTracePathJSONArray(issueLine, jsonArray);
        assertEquals(2, jsonArray.length());

    }

    @Test
    public void getTracePathJSONArray_wrongColumnNumber_expectRuntimeException() throws IOException {
        Assertions.assertThrows(FormatException.class, () -> {
            String issueRowWithWrongColNum = "2,5,68,2,5,0,1,2,7,0,24,2,7,0,26,2,7,0,17,2,11,0";
            CsfReaderV08 csfReader = CsfReaderV08.getInstance();
            String[] issueLine = issueRowWithWrongColNum.split(",");
            JSONArray jsonArray = new JSONArray();
            jsonArray = csfReader.getTracePathJSONArray(issueLine, jsonArray);
//
        });


    }


    @Test
    public void testgetAllIssueGroup_normal_expectCorrectCount() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        long issueTableStartOffset = csfReader.getNewIssueGroupTableStartOffset(issueFile);
        long issueTableEndOffset = csfReader.getNewIssueGroupTableEndOffset(issueFile);
        long stringTableStart = csfReader.getStringTableStartOffset(issueFile);
        long issueKeyTableStart = csfReader.getIssueKeyTableStartOffset(issueFile);
        long currentOffset = issueTableStartOffset;

        int count = 0;
        while (currentOffset < issueTableEndOffset) {
            String[] issueGroupCols = csfReader.getColsWithOffset(issueFile, 0, currentOffset);
            currentOffset = issueFile.getFilePointer();
            String functionName = csfReader.getLineWithOffset(issueFile, stringTableStart, csfReader.getIssueGroupFunctionNameOffset(issueGroupCols), 10);
            String issueKey = csfReader.getLineWithOffset(issueFile, issueKeyTableStart, csfReader.getIssueGroupKeyOffset(issueGroupCols), 10);
            ;
            System.out.println("----\n" + issueGroupCols);
            System.out.println(issueKey);
            System.out.println(functionName);
            count++;

        }
        assertEquals(2, count);
    }

    @Test
    public void testgetAllIssue_normal_expectCorrectCount() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        long issueGroupTableStartOffset = csfReader.getNewIssueGroupTableStartOffset(issueFile);
        long issueGroupTableEndOffset = csfReader.getNewIssueGroupTableEndOffset(issueFile);
        long stringTableStart = csfReader.getStringTableStartOffset(issueFile);
        long currentOffset = issueGroupTableStartOffset;

        int count = 0;
        while (currentOffset < issueGroupTableEndOffset) {
            String issueLine = csfReader.getLineWithOffset(issueFile, 0, currentOffset);
            System.out.println(issueLine);
            String[] issueLineCols = csfReader.getColsWithOffset(issueFile, 0, currentOffset);
            currentOffset = issueFile.getFilePointer();
            String functionName = csfReader.getLineWithOffset(issueFile, stringTableStart, csfReader.getIssueGroupFunctionNameOffset(issueLineCols), 10);
            System.out.println("----\n" + issueLineCols);
            System.out.println(functionName);
            count++;

        }
        assertEquals(2, count);
    }


    @Test
    public void testGetAllFileWithOffset_normal_expectCorrectCount() throws IOException {

        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        long filePathTableStartOffset = csfReader.getFilePathTableStartOffset(issueFile);
        long filePathTableEndOffset = csfReader.getFilePathTableEndOffset(issueFile);
        System.out.println(filePathTableStartOffset);
        System.out.println(filePathTableEndOffset);
        issueFile.seek(filePathTableStartOffset + 2);
        int count = 0;
        while (issueFile.getFilePointer() < filePathTableEndOffset) {
            long offset = issueFile.getFilePointer();
            String scanFilePath = csfReader.getLineWithOffset(issueFile, 0, offset);
            System.out.println(offset + " " + scanFilePath + " " + filePathTableEndOffset);
            count++;
        }
        assertEquals(26, count);
    }


    @Test
    public void getLongLittleEndian_normal_expectCorrectValue() throws IOException {

        byte[] bytes = new byte[]{(byte) 0x42, (byte) 0x9b, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        long value = csfReader.getLongLittleEndian(bytes);
        assertEquals(1088322, value);
    }

    @Test
    public void getIntLittleEndian_normal_expectCorrectValue() throws IOException {

        byte[] bytes = new byte[]{(byte) 0x42, (byte) 0x9b, (byte) 0x10, (byte) 0x00};
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        int value = csfReader.getIntLittleEndian(bytes);
        assertEquals(1088322, value);
    }

    @Test
    public void getSupportedMagic_normal_expectCorrectValue() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        assertEquals("XC5", csfReader.getSupportedMagic());
    }

    @Test
    public void getSupportedVersion_normal_expectCorrectValue() throws IOException {
        CsfReaderV08 csfReader = CsfReaderV08.getInstance();
        assertEquals("0.8", csfReader.getSupportedVersion());
    }




}
