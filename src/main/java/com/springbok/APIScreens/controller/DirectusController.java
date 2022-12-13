package com.springbok.APIScreens.controller;

import com.springbok.APIScreens.model.*;
import com.springbok.APIScreens.service.DirectusService;
import com.springbok.APIScreens.service.WeatherService;
import nonapi.io.github.classgraph.utils.URLPathEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

@RestController
@RequestMapping(value = "/", produces = "application/json")
public class DirectusController {

    @Autowired
    private DirectusService directusService;
    @Autowired
    private WeatherService weatherService;
    @GetMapping(value = "/persons")
    public String getAllPersons(@RequestParam(required = false) String showFields, @RequestParam(required = false) String startDate, HttpServletResponse response) throws IOException {
      if ((showFields == null || showFields.isEmpty()) && (startDate == null || startDate.isEmpty())){
        return directusService.findAllPersons(response);
      }
      else {
        return directusService.findAllPersonsFiltered(response, showFields, startDate);
      }

    }
    @GetMapping("/news")
    public NewsRoot getAllNews(){
      return directusService.findAllNews();
    }
    @GetMapping("/weather")
    public String getAllWeather(HttpServletResponse response) throws IOException {
      return directusService.findAllWeather();
    }
    @GetMapping("/weather/{id}")
    public String getSpecificWeather(HttpServletResponse response, @PathVariable("id") Integer id) throws IOException{
      return directusService.findSpecificWeather(id, response);
    }
    @GetMapping("/persons/{id}")
    public String getOnePerson(@PathVariable("id") String id, @RequestParam(required = false) String showFields, HttpServletResponse response)
      throws IOException {
      return directusService.findOnePerson(id, response, showFields);
    }
    @GetMapping("/weatherapi/{city}")
  public String getWeatherApi(HttpServletResponse response, @PathVariable("city") String city) throws IOException{
      return weatherService.getWeatherApi(response, city);
    }


}
