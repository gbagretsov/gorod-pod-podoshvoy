package com.company;
import java.io.FileNotFoundException;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        CITY mycity = new CITY(5);
        Hashtable<String, Integer> table =  Algorythm.dejkstra(1, mycity);
        System.out.print(table);
        jade.Boot.main(new String[]{"-gui","initializator:com.company.Agent_Initialize"});

    }
}
