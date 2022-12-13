package com.springbok.APIScreens.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springbok.APIScreens.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;

@Service
public class DirectusService {
  @Autowired
    private RestTemplate template = new RestTemplate();
      public String findAllPersons(HttpServletResponse response) throws JsonProcessingException {
        try {
          String fields = "";
          PersonRoot personRoot = getPersonRoot(fields);
          ObjectMapper objectMapper = new ObjectMapper();
          ArrayList<ObjectNode> personsArray = new ArrayList<>();
          for (int i = 0; i < personRoot.data.toArray().length; i++) {
            ObjectNode personObject = objectMapper.createObjectNode();
            Person personShort = personRoot.data.get(i);
            personObject.put("id", personShort.id);
            personObject.put("name", personShort.firstName + " " + personShort.lastName);
            personObject.put("function", personShort.function);
            personObject.put("img", personShort.img);
            personObject.put("phoneNumber", personShort.phonenumber);
            personObject.put("startdate", personShort.startDate);
            personObject.put("startdateformatted", dateFormatter(personShort));
            ObjectNode funFactArray = objectMapper.createObjectNode();
            funFactExtracted(personShort, funFactArray);
            if (!funFactArray.isEmpty()) {
              personObject.put("funfacts", funFactArray);
            }
            personsArray.add(personObject);
          }
          return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(personsArray);
        }
        catch (HttpClientErrorException e) {
          return getErrorString(response, e);
        }
      }
      public String findAllPersonsFiltered(HttpServletResponse response, String showFields, String startDate) throws IOException{
        try {
          if (showFields == null){
            showFields = "id,name,startdate,img,phonenumber,function,funfacts";
          }
            String fields = showFields.toLowerCase();
            if (showFields.toLowerCase().contains("name"))
              fields += ",firstName,lastName";
            if (showFields.toLowerCase().contains("startdate") || (startDate != null && !startDate.isEmpty())) {
              fields += ",startDate";
            }
            if (showFields.toLowerCase().contains("funfacts")) {
              fields += ",funFact1,funFact2,funFact3";
            }
            PersonRoot personRoot = getPersonRoot(fields);
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayList<ObjectNode> personsArray = new ArrayList<>();
            boolean dateShort = startDate != null && !startDate.isEmpty();
            for (int i = 0; i < personRoot.data.toArray().length; i++) {
              if (dateShort && !startDate.equals(personRoot.data.get(i).startDate)) {
              } else {
                ObjectNode personObject = objectMapper.createObjectNode();
                Person personShort = personRoot.data.get(i);
                ObjectNode funFactArray = objectMapper.createObjectNode();
                if (showFields.toLowerCase().contains("id")) {
                  personObject.put("id", personShort.id);
                }
                if (showFields.toLowerCase().contains("name")) {
                  personObject.put("name", personShort.firstName + " " + personShort.lastName);
                }
                if (showFields.toLowerCase().contains("function")) {
                  personObject.put("function", personShort.function);
                }
                if (showFields.toLowerCase().contains("img")) {
                  personObject.put("img", personShort.img);
                }
                if (showFields.toLowerCase().contains("phonenumber")) {
                  personObject.put("phoneNumber", personShort.phonenumber);
                }
                if (showFields.toLowerCase().contains("startdate")) {
                  personObject.put("startdate", personShort.startDate);
                  personObject.put("startdateformatted", dateFormatter(personShort));
                }
                if (showFields.toLowerCase().contains("funfacts")) {
                  funFactExtracted(personShort, funFactArray);
                  personObject.put("funfacts", funFactArray);
                }
                personsArray.add(personObject);
              }
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(personsArray);
          }
        catch (HttpClientErrorException e) {
          return getErrorString(response, e);
        }
    }
  public NewsRoot findAllNews(){
      return template.getForObject("https://cuxhsjf3.directus.app/items/news", NewsRoot.class);
    }
    public String findAllWeather() throws IOException{
        try {
          String url = "https://cuxhsjf3.directus.app/items/weathercode";
          WeatherRoot weatherroot = template.getForObject(url, WeatherRoot.class);
          ObjectMapper objectMapper = new ObjectMapper();
          ArrayNode weathermapped = objectMapper.createArrayNode();
          for (Weather weathercode : weatherroot.data) {
            ObjectNode weather = objectMapper.createObjectNode();
            weather.put("id", weathercode.id);
            weather.put("weathercode", weathercode.weathercode);
            weather.put("interpretation", weathercode.interpretation);
            weather.put("img", weathercode.img);
            weather.put("quote", weathercode.quote);
            weathermapped.add(weather);
          }
          return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(weathermapped);
        }
        catch (HttpClientErrorException e) {

          return null;
        }
    }
    public String findOnePerson(String id, HttpServletResponse response,@RequestParam(required = false) String showFields) throws IOException {
      try {
        String url = "https://cuxhsjf3.directus.app/items/blog/" + URLEncoder.encode(id, Charset.defaultCharset());
        PersonRoot2 person = template.getForObject(url, PersonRoot2.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode persons = objectMapper.createObjectNode();
        if (showFields.contains("id") || showFields.isEmpty()) {
          assert person != null;
          persons.put("id", person.data.id);
        }
        if (showFields.contains("name") || showFields == "" || showFields.isEmpty()) {
          assert person != null;
          persons.put("name", person.data.firstName + ' ' + person.data.lastName);
        }
        if (showFields.contains("img") || showFields == "" || showFields.isEmpty()){
          assert person != null;
          persons.put("img", person.data.img);
        }
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(persons);
      }
      catch (HttpClientErrorException e) {
        return getErrorString(response, e);
      }
    }
  public String findSpecificWeather(Integer id, HttpServletResponse response) throws IOException {
    try {
      String url = "https://cuxhsjf3.directus.app/items/weathercode";
      WeatherRoot weatherroot = template.getForObject(url, WeatherRoot.class);
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode weather = objectMapper.createObjectNode();
      for (Weather specificWeather:weatherroot.data) {
        if (Objects.equals(specificWeather.weathercode, id.toString())){
          weather.put("id", specificWeather.id);
          weather.put("weathercode", specificWeather.weathercode);
          weather.put("img", specificWeather.img);
          weather.put("interpretation", specificWeather.interpretation);
          weather.put("quote", specificWeather.quote);
        }
      }
      if (weather.isEmpty()){
        weather.put("id", "Sorry the requested id does not exist");
        weather.put("weathercode", "Existing weathercodes: 0,1,2,3,45,48,51,53,55,56,57,61,63,65,66,67,71,73,75,77,80,81,82,85,86,95,96,99");
        weather.put("More info", "For more info, go to open-meteo.com");
      }
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(weather);
    }
    catch (HttpClientErrorException e) {
      return getErrorString(response, e);
    }
  }

  public String findSpecificWeather1(Integer id) throws IOException {
    try {
      String url = "https://cuxhsjf3.directus.app/items/weathercode";
      WeatherRoot weatherroot = template.getForObject(url, WeatherRoot.class);
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode weather = objectMapper.createObjectNode();
      for (Weather specificWeather:weatherroot.data) {
        if (Objects.equals(specificWeather.weathercode, id.toString())){
          weather.put("id", specificWeather.id);
          weather.put("weatherId", specificWeather.weathercode);
          weather.put("weatherImg", specificWeather.img);
          weather.put("interpretation", specificWeather.interpretation);
          weather.put("quote", specificWeather.quote);
        }
      }
      if (weather.isEmpty()){
        weather.put("id", "Sorry the requested id does not exist");
        weather.put("weathercode", "Existing weathercodes: 0,1,2,3,45,48,51,53,55,56,57,61,63,65,66,67,71,73,75,77,80,81,82,85,86,95,96,99");
        weather.put("More info", "For more info, go to open-meteo.com");
      }
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(weather);
    }
    catch (HttpClientErrorException e) {
      return null;
    }
  }

  private PersonRoot getPersonRoot(String fields) {
    String url = "https://cuxhsjf3.directus.app/items/blog?fields="+fields;
    return template.getForObject(url, PersonRoot.class);
  }
  private static String dateFormatter(Person personShort) {
    String[] startDate = personShort.startDate.split("-");
    return startDate[2] + "-" + startDate[1] + "-" + startDate[0];
  }
  public static String getErrorString(HttpServletResponse response, HttpClientErrorException e) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode error = objectMapper.createObjectNode();
    error.put("ERROR", "Something went wrong");
    error.put("Message", e.getMessage());
    error.put("Status", e.getStatusCode().value());
    response.setStatus(e.getStatusCode().value());
    return error.toString();
  }
  private static void funFactExtracted(Person personShort, ObjectNode funFactArray) {
    if (personShort.funFact1 != null){
      funFactArray.put("funFact1", personShort.funFact1);
    }
    if (personShort.funFact2 != null){
      funFactArray.put("funFact2", personShort.funFact2);
    }
    if (personShort.funFact3 != null){
      funFactArray.put("funFact3", personShort.funFact3);
    }
  }


}


