package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

class CITY {

    //______ поля _______
    private String path = "matrix.txt";
    private Integer[][] MyCity = null;

    // глобальный счётчик id
    private static int currentID = 0;

    // статистика
    private static int totalCars = 0;
    private static int currentCars = 0;
    private static long totalTripTime = 0;
    private static int totalRoadsPassed = 0;
    private static int longestPathLength = 0;
    private static String longestPathCarName;
    private static int failedCars = 0;
    private static int maxLoad = 0;
    private static String busiestTL;


    //_______ конструктор __________
    public CITY(String path) throws FileNotFoundException {
        this.path = path;
        INPUT ();
    }
    public CITY() throws FileNotFoundException {
        INPUT ();
    }
    //___________________ методы _____________________________
    public void INPUT () throws FileNotFoundException {
       Scanner scan = new Scanner (new File(this.path));
       ArrayList<Integer> Traffic_Light_ID = new ArrayList<Integer>();

       while (scan.hasNextInt())
          Traffic_Light_ID.add(scan.nextInt());

        Double  citysize_double = Math.sqrt(Traffic_Light_ID.size());
        Integer citysize = citysize_double.intValue();
        this.MyCity = new Integer[citysize][citysize];

        int counter = 0;
        for (int i = 0; i < citysize; i++)
        {
            for (int j = 0; j < citysize; j++)
            {
                this.MyCity[i][j] = Traffic_Light_ID.get(counter);
                counter ++;
            }
        }
    }
    //public bool road_exists (int id_1, int id_2) {}
    public Integer[][] getMap (){
        return this.MyCity;
    }

    public static int getNextID() {
        return ++currentID;
    }

    public static void increaseCarsAmount() {
        totalCars++;
        currentCars++; // только что созданная машина является в том числе текущей
    }

    public static int getCurrentCarsAmount() {
        return currentCars;
    }

    public static void handleCarFinish(String carName, double tripTime, int pathLength) {
        currentCars--;
        totalTripTime += tripTime;
        if (pathLength > longestPathLength) {
            longestPathLength = pathLength;
            longestPathCarName = carName;
        }
    }

    public static void handleCarFailure() {
        currentCars--;
        failedCars++;
    }

    public static void increaseRoadsPassed() {
        totalRoadsPassed++;
    }

    public static void setMaxLoadIfNeeded(int load, String TLName) {
        if (load > maxLoad) {
            maxLoad = load;
            busiestTL = TLName;
        }
    }

    public static void printStatistics() {
        System.out.println("Total cars: " + totalCars);
        System.out.println("Total trip time: " + (double) totalTripTime / 1000 + " seconds");
        System.out.println("Average trip time: " + (double) totalTripTime / 1000 / (totalCars - failedCars) + " seconds");
        System.out.println("Total roads passed: " + totalRoadsPassed);
        System.out.println("Average path length: " + (double) totalRoadsPassed / (totalCars - failedCars));
        System.out.println("Car " + longestPathCarName + " had the longest path, its length is " + longestPathLength);
        if (failedCars > 0) System.out.println(failedCars + " cars didn't reach destinaton");
        System.out.println("Busiest traffic light is " + busiestTL + " - it had " + maxLoad + " cars to handle at the same time");
    }

}


