package com.company;

import java.util.ArrayList;

/**
 * Created by Nshindarev on 24.11.2015.
 */
public class GoThroughCity {
        public ArrayList<Integer> Local_Visited;
        private ArrayList<String> path;
        //private ArrayList<Integer> CantGoThere;
        public GoThroughCity(ArrayList<String> path){
            Local_Visited = new ArrayList<Integer>();
            //this.CantGoThere = CantGoThere;
            this.path = path;
        }
        public void collect_connected (Integer[][] City, Integer current) {
            for (Integer i = 0; i < City.length; i++)
                if ((City[current][i] != 0) && (!Local_Visited.contains(i)) && (!path.contains("tl_".concat(i.toString())))) {
                    Local_Visited.add(current);
                    collect_connected(City, i);
                }

        }
    }

