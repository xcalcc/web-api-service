package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssigneeCountRow {

    private UUID id;

    private String username;

    private String displayName;

    private String email;

    private String criticality;

    private String ruleCode;

    private Integer count;

}
