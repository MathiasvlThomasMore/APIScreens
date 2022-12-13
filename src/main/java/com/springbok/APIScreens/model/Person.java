package com.springbok.APIScreens.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
public class Person{
    public String id;
  @JsonProperty("user_created")
  @JsonAlias("user_creationDate")
  public String userCreated;
  public Date date_created;
  public String firstName;
    public String lastName;
    public String img;
    public String phonenumber;
    public String startDate;
    public String function;
    public String funFact1;
    public String funFact2;
    public String funFact3;
}

