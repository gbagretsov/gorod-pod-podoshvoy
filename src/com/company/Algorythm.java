package com.company;

import java.util.Hashtable;
public class Algorythm {
    // для реалистичности нужно добавить по секунде на размышление при каждом методе

    public static Hashtable<String, Integer> dijkstra(Integer icur, CITY City){

        // для хранения минимального расстояния
        Integer[] dijkstra = new Integer[City.city_size] ;

        //для проверки посещена ли вершина
        Integer cur = icur;
        boolean[] visited = new boolean[City.city_size];

        // инициализация массивов
        for (boolean b: visited) b = false;
        for (Integer i = 0; i < City.city_size; i++) dijkstra[i] = -1;

        // посетили входную вершину
        visited[icur] = true;
        dijkstra[icur] = 0;

        for (int i = 0; i< City.city_size; i++) // текущая вершина не совпадает с номером i
        {
            for(int j = 0; j< City.city_size; j++) // просматриваем всю строку
            {
                if (City.MyCity[cur][j] != 0) {
                    if (!visited[j])
                        if ((dijkstra[j] >= dijkstra[cur] + 1)) dijkstra[j] = dijkstra[cur]+1;
                        else if (dijkstra[j] == -1)
                            dijkstra[j] = dijkstra[cur] + 1;
                }
            }

            int min = cur;
            visited[cur] = true;

            for(int j = 0; j< City.city_size; j++) // просматриваем всю строку
            {
                if ( (!visited[ j]) && ((dijkstra[j] < dijkstra[min])||(min == cur)) && ( dijkstra[j]!= -1)) min = j;
                cur = min;
            }
        }

        Hashtable<String, Integer> Dijkstra = new Hashtable<String, Integer> (City.city_size);
        for (Integer i=0; i<City.city_size; i++)
            Dijkstra.put("tl_"+ i.toString(), dijkstra[i]);

        return Dijkstra;
    }
    public static String GetNextTL (Integer icur, CITY City, Hashtable<String, Integer> Traffic_NextTL, Integer cars_created){

        Integer roads_counter = 0;
        //количество дорог в городе
        for (int i = 0; i< City.city_size; i++)
            for (int j=0; j<City.city_size; j++) {
                roads_counter += City.getMap()[i][j];
            }
        Hashtable<String, Integer> Dijkstra = dijkstra(icur, City);
        Integer min_way = Dijkstra.get("tl_0");

        // подсчет минимального маршрута
        for (Integer i=1; i< City.city_size; i++) {
            Integer dijkstra_i = Dijkstra.get("tl_".concat(i.toString()));
            if ((dijkstra_i < min_way)&&(dijkstra_i != 0))
                min_way = dijkstra_i;
        }

        // маршрут максимальной длины, который можно использовать
        Integer max_way_1 = min_way + 3;
        Integer max_way_2 = min_way + City.city_size/10;
        Integer max_way = (max_way_1 > max_way_2) ? max_way_1 : max_way_2;

        // заполняем таблицу со стоимостью дорог

        Hashtable<String, Integer> TL_Price = new Hashtable<String, Integer>();

        Integer k;
        String  min_key = "error";
        Integer min_koef = Integer.MAX_VALUE;

        for (Integer i=0; i<City.city_size; i++){
            if ( Dijkstra.get("tl_".concat(i.toString())) > 0) {
                k = Traffic_NextTL.get("tl_".concat(i.toString()))*2 + cars_created * Dijkstra.get("tl_".concat(i.toString())) / roads_counter;
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
