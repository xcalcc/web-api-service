package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueGroupSrcSinkFilePath {

    private String id;  // issue group id

    private String srcRelativePath;

    private String sinkRelativePath;
}
