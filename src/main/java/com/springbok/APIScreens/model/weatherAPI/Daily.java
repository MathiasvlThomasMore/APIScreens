package com.springbok.APIScreens.model.weatherAPI;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Daily {
  public List<Integer> weathercode;
  @JsonProperty("temperature_2m_max")
  public List<Double> temperatureMax;
  @JsonProperty("temperature_2m_min")
  public List<Double> temperatureMin;
  @JsonProperty("rain_sum")
  public List<Double> rainSum;
  @JsonProperty("winddirection_10m_dominant")
  public List<Integer> winddirection;
}
