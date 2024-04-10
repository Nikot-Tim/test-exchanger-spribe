package com.spribe.exchanger.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationDetail {

  private String field;
  private String message;
}
