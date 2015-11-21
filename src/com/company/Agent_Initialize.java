package com.company;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.StaleProxyException;

import java.io.FileNotFoundException;

public class Agent_Initialize extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new OneShotBehaviour(this) {

            CITY city;

            @Override
            public void action() {
                /* Создаём город */
                try {
                    city = new CITY(5);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //TODO: map = city.getMap
                int[][] map = new int[][] {
                        {0, 1, 0, 1, 0},
                        {1, 0, 1, 0, 0},
                        {0, 0, 0, 1, 1},
                        {0, 1, 1, 0, 1},
                        {0, 0, 1, 0, 0}
                };

                try {
                    /* Создаём светофоры */
                    for (int i = 0; i < map.length; i++) {
                        Object[] args = new Object[2];
                        args[0] = map;
                        args[1] = i;

                        getContainerController()
                                .createNewAgent("tl_".concat(String.valueOf(i)), "com.company.Agent_TrafficLight", args)
                                .start();
                    }
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

        } );
    }
}
