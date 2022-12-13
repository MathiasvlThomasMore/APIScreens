package com.springbok.APIScreens.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Coordinates {
  private double latitude;
  private double longitude;
}
