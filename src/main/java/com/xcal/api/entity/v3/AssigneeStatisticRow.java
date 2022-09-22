package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssigneeStatisticRow {

    private User user;

    private Integer counts;

    private BreakDownByRiskAndCsvCode breakDownByRiskAndCsvCode;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BreakDownByRiskAndCsvCode {
        private Map<String, Integer> high;
        private Map<String, Integer> medium;
        private Map<String, Integer> low;
    }
}
