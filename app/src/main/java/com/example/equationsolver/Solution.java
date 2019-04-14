package com.example.equationsolver;

public class Solution {
    boolean success;
    String result, graph;

    public Solution(boolean success, String result, String graph) {
        this.success = success;
        this.result = result;
        this.graph = graph;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResult() {
        return result;
    }

    public String getGraph() {
        return graph;
    }
}
