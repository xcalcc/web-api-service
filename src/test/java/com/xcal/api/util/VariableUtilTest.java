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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VariableUtilTest {

    @Test
    void getEnumByCsfValue_0_returnSingle() {
        assertEquals(VariableUtil.ScanMode.SINGLE, VariableUtil.ScanMode.getEnumByCsfValue("0"));
    }

    @Test
    void getEnumByCsfValue_1_returnSingle() {
        assertEquals(VariableUtil.ScanMode.CROSS, VariableUtil.ScanMode.getEnumByCsfValue("1"));
    }

    @Test
    void getEnumByCsfValue_2_returnSingle() {
        assertEquals(VariableUtil.ScanMode.SINGLE_XSCA, VariableUtil.ScanMode.getEnumByCsfValue("2"));
    }

    @Test
    void getEnumByCsfValue_other_throwIllegualArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            VariableUtil.ScanMode.getEnumByCsfValue("other");
        });
    }

    @Test
    void getEnumByParamValue_single_returnSingle() {
        assertEquals(VariableUtil.ScanMode.SINGLE, VariableUtil.ScanMode.getEnumByParamValue("-single"));
    }

    @Test
    void getEnumByParamValue_cross_returnSingle() {
        assertEquals(VariableUtil.ScanMode.CROSS, VariableUtil.ScanMode.getEnumByParamValue("-cross"));
    }

    @Test
    void getEnumByParamValue_singleXSCA_returnSingle() {
        assertEquals(VariableUtil.ScanMode.SINGLE_XSCA, VariableUtil.ScanMode.getEnumByParamValue("-single-xsca"));
    }

    @Test
    void getEnumByParamValue_other_throwIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            VariableUtil.ScanMode.getEnumByParamValue("other");
        });
    }

    @Test
    void getEnumByName_single_returnSingle() {
        assertEquals(VariableUtil.ScanMode.SINGLE, VariableUtil.ScanMode.getEnumByName("SINGLE"));
    }

    @Test
    void getEnumByName_cross_returnSingle() {
        assertEquals(VariableUtil.ScanMode.CROSS, VariableUtil.ScanMode.getEnumByName("CROSS"));
    }


    @Test
    void getEnumByName_singleXSCA_returnSingle() {
        assertEquals(VariableUtil.ScanMode.SINGLE_XSCA, VariableUtil.ScanMode.getEnumByName("SINGLE_XSCA"));
    }


    @Test
    void getEnumByName_other_throwIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            VariableUtil.ScanMode.getEnumByName("other");
        });
    }
}