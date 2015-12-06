package com.company;

import javax.xml.bind.helpers.ParseConversionEventImpl;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Scanner;

public class Algorythm {

    /*
    Метод dijkstra
       на входе имеем номер текущей вершины, у которой стоим в очереди
       на выходе получаем хеш-таблицу расстояний от текущей вершины до каждого из светофоров
    */
    /*
     Метод GetNextTL
     --- i_cur_string -> id светофора, у которого находится агент
     --- city -> город
     --- Traffic_NextTL -> хеш-таблица, с информацией о количестве автомобилей
                           у соседних светофоров, которая поступает от агентов
     --- cars_created -> количество агентов автомобилей, которые мы сгенерировали
  */


    public static Integer cars_created = 0;
    public static Hashtable<String, Integer> dijkstra(Integer icur, Integer[][] City){

        // для хранения минимального расстояния
        Integer[] dijkstra = new Integer[City.length] ;

        //для проверки посещена ли вершина
        Integer cur = icur;
        boolean[] visited = new boolean[City.length];

        // инициализация массивов
        for (Integer i = 0; i < City.length; i++) visited[i] = false;
        for (Integer i = 0; i < City.length; i++) dijkstra[i] = Integer.MAX_VALUE;

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
                }
            }

            int min = cur;
            visited[cur] = true;

            for(int j = 0; j< City.length; j++) // просматриваем всю строку
            {
                if ((!visited[ j]) && ((dijkstra[j] < dijkstra[cur])||(dijkstra[cur]==0)||(visited[cur])) && (dijkstra[j]< Integer.MAX_VALUE))
                    cur = j;
            }
        }

        Hashtable<String, Integer> Dijkstra = new Hashtable<String, Integer> (City.length);
        for (Integer i=0; i<City.length; i++)
            Dijkstra.put("tl_"+ i.toString(), dijkstra[i]);

        return Dijkstra;
    }
    public static String GetNextTL (String i_cur_string, Integer[][] City, Hashtable<String, Integer> Traffic_NextTL,
                                    ArrayList<String> path, ArrayList<String> cantgohere, Integer finish,
                                    Queue<Integer> key_memory, Integer[][] value_memory){

        /* выбираем номер нашего светофора для дейкстры */
        String MyTL = new String(i_cur_string.replaceAll("tl_", ""));
        Integer icur = Integer.parseInt (MyTL);
        Integer roads_counter = 0;

        //количество дорог в городе
        for (int i = 0; i< City.length; i++)
            for (int j=0; j<City.length; j++) {
                roads_counter += City[i][j];
            }
        Hashtable<String, Integer> Dijkstra ;
        Integer min_way;
        min_way = 0;

        // подсчет минимального маршрута
        // просматриваем путь от смежных до финиша
        for (Integer i=1; i< City.length; i++) {
            if(City[icur][i] > 0) {
                Dijkstra = dijkstra(i, City);
              Integer dijkstra_i = Dijkstra.get("tl_".concat(finish.toString()))+1;
              if ((dijkstra_i < min_way)||(min_way == 0))
                  min_way = dijkstra_i;
            }
        }

        // маршрут максимальной длины, который можно использовать
        Integer max_way_1 = min_way + 3;
        Integer max_way_2 = min_way + City.length/6;
        Integer max_way = (max_way_1 > max_way_2) ? max_way_1 : max_way_2;

        // заполняем таблицу со стоимостью дорог
        Hashtable<String, Integer> TL_Price = new Hashtable<String, Integer>();

        Integer k;
        Integer delete_from_memory = null;
        String  min_key = "error";
        Integer min_koef = Integer.MAX_VALUE;



        for (Integer i=0; i<City.length; i++){
                Dijkstra = dijkstra(i, City);
                if (( City[icur][i] > 0)&& ((Dijkstra.get("tl_".concat(finish.toString()))+1) <= max_way)) {

                if(value_memory[icur][i]==1){
                  k = Traffic_NextTL.get("tl_".concat(i.toString()))*2 + (Dijkstra.get("tl_".concat(finish.toString()))+1)*3 ;
                    value_memory[icur][i] = k; }
                else k = value_memory[icur][i] + 3 + Traffic_NextTL.get("tl_".concat(i.toString()))*2;

                    // добавляем пробку в память авто
                    if (key_memory.contains(i) && key_memory.peek() != i) {
                        key_memory.remove(i);
                        key_memory.add(i);
                    }
                    else if (!key_memory.contains(i)) {
                        key_memory.add(i);
                    }
                    if (key_memory.size() >= 5){
                      delete_from_memory =  key_memory.poll();
                      for (Integer j = 0; j< City.length; j++) {
                          if (value_memory[delete_from_memory][i] > 1)
                              value_memory[delete_from_memory][i] = 1;
                      }
                    }


                if ((k < min_koef)
                       // &&(!cantgohere.contains("tl_".concat(i.toString())))
                    ) {
                    min_koef = k;
                    min_key = "tl_".concat(i.toString());
                }
            }
                else k = Integer.MAX_VALUE;

            TL_Price.put("tl_".concat(i.toString()), k);
        }
        return  min_key;
    }
    public static ArrayList<String> CantGoThere (String current, Integer[][] City, ArrayList<String> path, Integer finish) {
        /* Номер нашего светофора */
        String MyTL = new String(current.replaceAll("tl_", ""));
        Integer icur = Integer.parseInt (MyTL);



        /* переводим посещенные id в номера */
        ArrayList<Integer> TL_Number_Path = new ArrayList<Integer> (path.size());
        String myTL;
        Integer mycur;
        for (Integer i = 0; i< path.size(); i++) {
            myTL = new String(path.get(i).replaceAll("tl_", ""));
            mycur = Integer.parseInt (myTL);
            TL_Number_Path.add(mycur);
        }

        /* теперь удаляем текущую вершину и заполняем таблицу с компонентами связности */
        /* учитывать будем только те дороги, которые ведут в компоненту с финишной вершиной */
        /* стартуем с вершин, в которые можно прийти из текущей */


        ArrayList<ArrayList<Integer>> Connected_components = new ArrayList<ArrayList<Integer>>();

        Integer jj = 0;
        //запишем во все списки по одной смежной вершине с исходной
        for (Integer i=0; i < City.length; i++){
            if (City[icur][i] != 0) {
                Connected_components.add(new ArrayList<Integer>());
                Connected_components.get(jj).add(i);
                jj++;
            }
        }

        //добавляем текущую в список тех, где побывали, чтобы ее игнорировать

           if(!path.contains("tl_".concat(icur.toString())))
               path.add("tl_".concat(icur.toString()));
            if (!TL_Number_Path.contains(icur))  TL_Number_Path.add(icur);


        // совершаем поиск в ширину для каждой из смежных с текущей
        for(Integer i=0; i<Connected_components.size(); i++)
        {
            GoThroughCity OneConnectedComponent= new GoThroughCity(path);
            for (Integer x = 0; x < GoThroughCity.adjacent_counter (City, i); x++)
              OneConnectedComponent.collect_connected(City, Connected_components.get(i).get(0));
            for (Integer j : OneConnectedComponent.Local_Visited)
                  Connected_components.get(i).add(j);
        }
        int Index = 0;
        ArrayList<String> cant = new ArrayList<String>();
        for (Integer i = 0; i < Connected_components.size(); i++){
            if(!Connected_components.get(i).contains(finish))
                cant.add("tl_".concat(Connected_components.get(i).get(Index).toString()));

        }
        return  cant;

    }

}
