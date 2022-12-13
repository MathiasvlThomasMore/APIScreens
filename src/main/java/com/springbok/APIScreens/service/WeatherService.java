package com.springbok.APIScreens.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springbok.APIScreens.model.Coordinates;
import com.springbok.APIScreens.model.Weather;
import com.springbok.APIScreens.model.weatherAPI.CurrentWeather;
import com.springbok.APIScreens.model.weatherAPI.Daily;
import com.springbok.APIScreens.model.weatherAPI.WeatherAPIRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;

import static com.springbok.APIScreens.service.DirectusService.getErrorString;
@Service
public class WeatherService {
  @Autowired
  private RestTemplate template = new RestTemplate();

  public HashMap<String, Coordinates> cities(){
    HashMap<String , Coordinates> coordinatesHashMap = new HashMap<>();
    coordinatesHashMap.put("Mechelen",
      Coordinates.builder()
        .latitude(51.03)
        .longitude(4.48)
        .build());
    coordinatesHashMap.put("Brussels",
      Coordinates.builder()
        .latitude(50.85)
        .longitude(4.35)
        .build());
    coordinatesHashMap.put("Antwerp",
      Coordinates.builder()
        .latitude(51.22)
        .longitude(4.40)
        .build());
    coordinatesHashMap.put("Amsterdam",
      Coordinates.builder()
        .latitude(52.37)
        .longitude(4.89)
        .build());
    coordinatesHashMap.put("DenBosch",
      Coordinates.builder()
        .latitude(51.70)
        .longitude(5.30)
        .build());
    return coordinatesHashMap;
  }
  public String getWeatherApi(HttpServletResponse response, String city) throws IOException {
    try {
      //Setup
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode weatherApiCity = objectMapper.createObjectNode();
      if (cities().get(city) != null) {
        //All cities get put in an array
        Coordinates coordinates = cities().get(city);
        String url = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=weathercode,temperature_2m_max,temperature_2m_min,rain_sum,winddirection_10m_dominant&current_weather=true&timezone=Europe/Berlin", coordinates.getLatitude(), coordinates.getLongitude());

        //For every city we want current weather and daily weather
        //Setup per city
        WeatherAPIRoot weather = template.getForObject(url, WeatherAPIRoot.class);

        //Current Weather
        ObjectNode currentWeather = objectMapper.createObjectNode();
        ObjectNode directusWeather = objectMapper.createObjectNode();
        assert weather != null;
        //Everything that has to be put in currentWeather
        CurrentWeather weatherCurrent = weather.current_weather;
        currentWeather.put("temperature", weatherCurrent.temperature); //In °C
        currentWeather.put("windSpeed", weatherCurrent.windspeed); //In km/h
        currentWeather.put("windDirection", weatherCurrent.winddirection); //Integer between 0 and 360 (0 = North; Clockwise)
        //Current Weather Wind direction Short
        String[] windDirectionShort = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        for (int i = 0; i < 9; i++) {
          if (i * 45 - 22.5 <= weatherCurrent.winddirection && weatherCurrent.winddirection <= i * 45 + 22.5) {
            currentWeather.put("windDirectionShort", windDirectionShort[i]);
          }
        }
        currentWeather.put("weatherCode", weatherCurrent.weathercode); //See Open-meteo.com Documentation; Integer between 0 and 100
        //Connect weather api with weather icons & quotes from directus
        DirectusService directusServiceNow = new DirectusService();
        String allWeatherNow = directusServiceNow.findSpecificWeather1(weatherCurrent.weathercode);
        System.out.println(allWeatherNow);
        Weather weatherNow = objectMapper.readValue(allWeatherNow, Weather.class);
        directusWeather(directusWeather, weatherNow);
        currentWeather.put("directus", directusWeather);
        //Daily Weather for the next week
        ObjectNode dailyWeather = objectMapper.createObjectNode();
        Daily weatherDaily = weather.daily;
        ObjectNode weatherCode = objectMapper.createObjectNode();
        ObjectNode temperatureMax = objectMapper.createObjectNode();
        ObjectNode temperatureMin = objectMapper.createObjectNode();
        ObjectNode rainSum = objectMapper.createObjectNode();
        ObjectNode windDirection = objectMapper.createObjectNode();
        ObjectNode windDirectionAbbreviation = objectMapper.createObjectNode();
        ObjectNode directusWeatherDaily = objectMapper.createObjectNode();
        for (int i = 0; i < weatherDaily.weathercode.toArray().length; i++) {
          weatherCode.put(String.valueOf(i), weatherDaily.weathercode.get(i));
          temperatureMax.put(String.valueOf(i), weatherDaily.temperatureMax.get(i));
          temperatureMin.put(String.valueOf(i), weatherDaily.temperatureMin.get(i));
          rainSum.put(String.valueOf(i), weatherDaily.rainSum.get(i));
          windDirection.put(String.valueOf(i), weatherDaily.winddirection.get(i));
          for (int j = 0; j < 9; j++) {
            if (j * 45 - 22.5 <= weatherDaily.winddirection.get(i) && weatherDaily.winddirection.get(i) <= j * 45 + 22.5) {
              windDirectionAbbreviation.put(String.valueOf(i), windDirectionShort[j]);
            }
          }
          //Connect Weather api daily with weather icons & quotes from directus
          DirectusService directusService = new DirectusService();
          String allWeatherOtherDays = directusService.findSpecificWeather1(weatherDaily.weathercode.get(i));
          ObjectNode directusWeatherD = objectMapper.createObjectNode();
          Weather weatherOtherDays = objectMapper.readValue(allWeatherOtherDays, Weather.class);
          directusWeather(directusWeatherD, weatherOtherDays);
          directusWeatherDaily.put(String.valueOf(i), directusWeatherD);
        }
        //Everything that has to be put in dailyWeather
        dailyWeather.put("weatherCode", weatherCode);
        dailyWeather.put("temperatureMax", temperatureMax);
        dailyWeather.put("temperatureMin", temperatureMin);
        dailyWeather.put("rainSum", rainSum);
        dailyWeather.put("windDirection", windDirection);
        dailyWeather.put("windDirectionShort", windDirectionAbbreviation);
        dailyWeather.put("directus", directusWeatherDaily);
        //Everything that has to be returned
        weatherApiCity.put("currentWeather", currentWeather);
        weatherApiCity.put("dailyWeather", dailyWeather);
      }
      else {
        weatherApiCity.put("ERROR", "This city name does not exist");
        weatherApiCity.put("Did you try:", "Mechelen, Brussels, Den bosch, Antwerp or Amsterdam?");
      }
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(weatherApiCity);
    }
    catch (HttpClientErrorException e) {
      return getErrorString(response, e);
    }
  }

  private void directusWeather(ObjectNode directusWeather, Weather weatherRoot) {
    assert weatherRoot != null;
    directusWeather.put("id", weatherRoot.id);
    directusWeather.put("interpretation", weatherRoot.interpretation);
    directusWeather.put("img", weatherRoot.img);
    directusWeather.put("quote", weatherRoot.quote);
  }

}
