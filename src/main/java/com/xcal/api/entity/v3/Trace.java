package com.xcal.api.entity.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trace {

	@JsonProperty("fid")
	private Integer fileId;

	@JsonProperty("ln")
	private Integer lineNo;

	@JsonProperty("cn")
	private Integer columnNo;

	@JsonProperty("mid")
	private Integer msgId;

}
