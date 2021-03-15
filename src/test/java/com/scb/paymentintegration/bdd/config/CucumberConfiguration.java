package com.scb.paymentintegration.bdd.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;

import java.lang.reflect.Type;

public class CucumberConfiguration {
    private final ObjectMapper objectMapper;

    public CucumberConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @DefaultDataTableCellTransformer
    @DefaultDataTableEntryTransformer
    @DefaultParameterTransformer
    public Object transform(final Object from, Type type){
        return objectMapper.convertValue(from,objectMapper.constructType(type));
    }
}
