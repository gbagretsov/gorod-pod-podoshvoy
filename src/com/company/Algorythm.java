package com.company;

import javax.xml.bind.helpers.ParseConversionEventImpl;
import java.util.Hashtable;
import java.util.Scanner;

public class Algorythm {

    // для реалистичности нужно добавить по секунде на размышление при каждом методе
    public static Integer cars_created = 0;
    public static Hashtable<String, Integer> dijkstra(Integer icur, int[][] City){

        // для хранения минимального расстояния
        Integer[] dijkstra = new Integer[City.length] ;

        //для проверки посещена ли вершина
        Integer cur = icur;
        boolean[] visited = new boolean[City.length];

        // инициализация массивов
        for (boolean b: visited) b = false;
        for (Integer i = 0; i < City.length; i++) dijkstra[i] = -1;

        // посетили входную вершину
        visited[icur] = true;
        dijkstra[icur] = 0;

        for (int i = 0; i< City.length; i++) // текущая вершина не совпадает с номером i
        {
            for(int j = 0; j< City.length; j++) // просматриваем всю строку
            {
                if (City [cur][j] != 0) {
                    if (!visited[j])
                        if ((dijkstra[j] >= dijkstra[cur] + 1)) dijkstra[j] = dijkstra[cur]+1;
                        else if (dijkstra[j] == -1)
                            dijkstra[j] = dijkstra[cur] + 1;
                }
            }

            int min = cur;
            visited[cur] = true;

            for(int j = 0; j< City.length; j++) // просматриваем всю строку
            {
                if ( (!visited[ j]) && ((dijkstra[j] < dijkstra[min])||(min == cur)) && ( dijkstra[j]!= -1)) min = j;
                cur = min;
            }
        }

        Hashtable<String, Integer> Dijkstra = new Hashtable<String, Integer> (City.length);
        for (Integer i=0; i<City.length; i++)
            Dijkstra.put("tl_"+ i.toString(), dijkstra[i]);

        return Dijkstra;
    }
    /*
       Метод GetNextTL
       --- icur -> текущая вершина, у которой находится машина
       --- city -> город
       --- Traffic_NextTL -> хеш-таблица, с информацией о количестве автомобилей
                             у соседних светофоров, которая поступает от агентов
       --- cars_created -> количество агентов автомобилей, которые мы сгенерировали
    */
    public static String GetNextTL (String i_cur_string, int[][] City, Hashtable<String, Integer> Traffic_NextTL){

        String MyTL = new String(i_cur_string.replaceAll("tl_", ""));
        Integer icur = Integer.parseInt (MyTL);
        Integer roads_counter = 0;
        //количество дорог в городе
        for (int i = 0; i< City.length; i++)
            for (int j=0; j<City.length; j++) {
                roads_counter += City[i][j];
            }
        Hashtable<String, Integer> Dijkstra = dijkstra(icur, City);
        Integer min_way = Dijkstra.get("tl_0");

        // подсчет минимального маршрута
        for (Integer i=1; i< City.length; i++) {
            Integer dijkstra_i = Dijkstra.get("tl_".concat(i.toString()));
            if ((dijkstra_i < min_way)&&(dijkstra_i != 0))
                min_way = dijkstra_i;
        }

        // маршрут максимальной длины, который можно использовать
        Integer max_way_1 = min_way + 3;
        Integer max_way_2 = min_way + City.length/10;
        Integer max_way = (max_way_1 > max_way_2) ? max_way_1 : max_way_2;

        // заполняем таблицу со стоимостью дорог

        Hashtable<String, Integer> TL_Price = new Hashtable<String, Integer>();

        Integer k;
        String  min_key = "error";
        Integer min_koef = Integer.MAX_VALUE;

        for (Integer i=0; i<City.length; i++){
            if ( City[icur][i] > 0) {
                k = Traffic_NextTL.get("tl_".concat(i.toString()))*2 + Algorythm.cars_created * Dijkstra.get("tl_".concat(i.toString())) / roads_counter;
                if ((k < min_koef)&&(Dijkstra.get("tl_".concat(i.toString())) <= max_way)) {
                    min_koef = k;
                    min_key = "tl_".concat(i.toString());
                }
            }

                else k = Integer.MAX_VALUE;

            TL_Price.put("tl_".concat(i.toString()), k);
        }
        return  min_key;
    }

}
