package com.xcal.api.entity.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceCodeInfo {

	private UUID scanTaskId;

	private String fileStorageHost;

	private String relativePath;

}
