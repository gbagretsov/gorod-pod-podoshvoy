package com.company;
import java.io.FileNotFoundException;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        CITY mycity = new CITY(5);
        Hashtable<String, Integer> table =  Algorythm.dijkstra(1, mycity.getMap());
        System.out.print(table);


        Hashtable<String, Integer> Traffic_NextTL = new Hashtable<String, Integer> ();

        Traffic_NextTL.put("tl_4", 7);
        Traffic_NextTL.put("tl_3", 40);
        Traffic_NextTL.put("tl_2", 0);
        Traffic_NextTL.put("tl_1", 0);
        Traffic_NextTL.put("tl_0", 0);

      //  System.out.println("next traffic light is  " + Algorythm.GetNextTL (2, mycity, Traffic_NextTL, 200));
        jade.Boot.main(new String[]{"-gui","initializator:com.company.Agent_Initialize"});

    }
}
