package com.bezkoder.spring.datajpa;

public interface RequestDataFromHospital {
    String getDataFromHospital2Patient(String telephone, String hospital, String token);
    String saveDataFromHopital2Platform(String datainjson);

}
