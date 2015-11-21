package com.company;

import jade.core.Agent;

import java.util.*;


public class Agent_TrafficLight extends Agent {

    private Hashtable<String, LinkedList<Agent_Car>> cars;
    private List<String> outcoming;

    @Override
    protected void setup() {

        Object[] args = getArguments();
        int[][] map = (int[][]) args[0];
        int index = Integer.parseInt(args[1].toString());

        /* Создаём очереди */
        cars = new Hashtable<String, LinkedList<Agent_Car>>();
        for (int i = 0; i < map.length; i++) {
            if (map[i][index] == 1) {
                cars.put("tl_".concat(String.valueOf(i)), new LinkedList<Agent_Car>());
            }
        }

        /* Создаём список исходящих */
        outcoming = new ArrayList<String>();
        for (int i = 0; i < map.length; i++) {
            if (map[index][i] == 1) {
                outcoming.add("tl_".concat(String.valueOf(i)));
            }
        }

        /* Debug output */
        String incomingString = "", outcomingString = "";
        for (String s : cars.keySet()) {
            incomingString = incomingString.concat(s + " ");
        }
        for (String s : outcoming) {
            outcomingString = outcomingString.concat(s + " ");
        }
        System.out.println("Debug: " + getAID().getLocalName()
                + "; in: "  + incomingString
                + "; out: " + outcomingString);
    }

}
