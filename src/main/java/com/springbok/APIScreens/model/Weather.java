package com.springbok.APIScreens.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Weather{
  public String id;
  @JsonProperty("weatherId")
  public String weathercode;
  @JsonProperty("weatherImg")
  public String img;
  public String quote;
  public String interpretation;
}
