package com.example.ultimatemorsedecoder;

public class MorseDecoder{
    private int dataRemaining;
    private int dot, dash, letSep, groupSep;
    private double[] data;

    public MorseDecoder(int chunkSize) {
        data = new double[chunkSize];
        dataRemaining = 0;
    }
    String processNext(double[] L) {
        for (int i = 0; i<dataRemaining; ++i) {

        }
    }
}
