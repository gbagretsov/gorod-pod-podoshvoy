package com.company;

import java.util.Hashtable;
public class Algorythm {
    public static Hashtable<String, Integer> dejkstra (Integer icur,  CITY City){

        // для хранения минимального расстояния
        Integer[] dejkstra = new Integer[City.city_size] ;

        //для проверки посещена ли вершина
        Integer cur = icur;
        boolean[] visited = new boolean[City.city_size];

        // инициализация массивов
        for (boolean b: visited) b = false;
        for (Integer i = 0; i < City.city_size; i++) dejkstra[i] = -1;

        // посетили входную вершину
        visited[icur] = true;
        dejkstra[icur] = 0;

        for (int i = 0; i< City.city_size; i++) // текущая вершина не совпадает с номером i
        {
            for(int j = 0; j< City.city_size; j++) // просматриваем всю строку
            {
                if (City.MyCity[cur][j] != 0) {
                    if (!visited[j])
                        if ((dejkstra[j] >= dejkstra[cur] + 1)) dejkstra[j] = dejkstra[cur]+1;
                        else if (dejkstra[j] == -1)
                            dejkstra[j] = dejkstra[cur] + 1;
                }
            }

            int min = cur;
            visited[cur] = true;

            for(int j = 0; j< City.city_size; j++) // просматриваем всю строку
            {
                if ( (!visited[j]) && ((dejkstra[j] < dejkstra[min])||(min == cur)) && ( dejkstra[j]!= -1)) min = j;
                cur = min;
            }
        }

        Hashtable<String, Integer> Dejkstra = new Hashtable<String, Integer> (City.city_size);
        for (Integer i=0; i<City.city_size; i++)
            Dejkstra.put("tl_"+ i.toString(), dejkstra[i]);

        return Dejkstra;
    }
}
