package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class CITY {

    //______ поля _______
    private String path = "C://ForFileManager//MAC.txt";
    private int city_size;
    private int[][] CityArray = null;

    //_______ конструктор __________
    public CITY(int size) throws FileNotFoundException {

        this.INPUT (size);
        this.city_size = size;
    }

    //___________________ методы _____________________________
    public void INPUT (int size) throws FileNotFoundException {

        CityArray = new int[size][size];
        Scanner sc = new Scanner(new File(this.path));
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                this.CityArray[i][j] = sc.nextInt();
            }
        }
    }
    //public bool road_exists (int id_1, int id_2) {}
    public int[][] getMap (){
        return this.CityArray;
    }
}


