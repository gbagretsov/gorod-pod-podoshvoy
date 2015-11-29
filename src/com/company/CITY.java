package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

class CITY {

    //______ поля _______
    private String path = "C://ForFileManager//matrix.txt";
    private Integer[][] MyCity = null;
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
}


