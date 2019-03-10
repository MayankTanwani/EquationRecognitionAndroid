package com.example.equationsolver;

public class Equation {
    String equation;
    int result;

    public Equation(String equation, int result) {
        this.equation = equation;
        this.result = result;
    }

    public String getEquation() {
        return equation;
    }

    public int getResult() {
        return result;
    }
}
