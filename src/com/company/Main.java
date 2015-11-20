package com.company;

import java.io.*;
import java.util.Scanner;

class CITY {

    //______ поля _______
    public int city_size;
    public int[][] MyCity;

    //_______ конструктор __________
    public CITY(int size) throws FileNotFoundException {

        this.INPUT (size);
        this.city_size = size;
    }

    //___________________ методы _____________________________
    public void INPUT (int size) throws FileNotFoundException {

        MyCity = new int[size][size];
        String path = "C://ForFileManager//MAC.txt";
        Scanner sc = new Scanner(new File(path));
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                this.MyCity[i][j] = sc.nextInt();
            }
        }
    }
    //public bool road_exists (int id_1, int id_2) {}
    //public int get_road_length (int id_1, int id_2) {}
}

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        CITY mycity = new CITY(4);
      /*  for (int i = 0; i< mycity.city_size; i++)
            for (int j = 0; j< mycity.city_size; j++)
                System.out.println(mycity.MyCity[i][j]);
      */
    }
}
