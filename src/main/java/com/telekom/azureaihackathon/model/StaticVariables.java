package com.telekom.azureaihackathon.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StaticVariables {
    public static Query context = new Query(new ArrayList<>(), 0.7);
    public static List<String> businessRequirements = new ArrayList<>();

    public static void cleanContext() {
        context = new Query(new ArrayList<>(), 0.7);
        businessRequirements = new ArrayList<>();
        businessRequirements.add("Here is a list of business requirements for analysis of further user stories"); //TODO: fix this with better solution
    }

}
