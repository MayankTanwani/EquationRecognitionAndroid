package com.example.equationsolver;

public class Equation {
    String imgUri, equation;

    public Equation(String imgUri, String equation) {
        this.imgUri = imgUri;
        this.equation = equation;
    }

    public String getImgUri() {
        return imgUri;
    }

    public String getEquation() {
        return equation;
    }
}
