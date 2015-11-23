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
                int[][] map = city.getMap();

                try {
                    /* Создаём светофоры. Аргументы: { карта, индекс } */
                    for (int i = 0; i < map.length; i++) {
                        Object[] args = new Object[2];
                        args[0] = map;
                        args[1] = i;

                        getContainerController()
                                .createNewAgent("tl_".concat(String.valueOf(i)), "com.company.Agent_TrafficLight", args)
                                .start();
                    }

                    /* Добавляем машины
                     * Аргументы: { beginFrom, beginTo } (машина начинает движение с дуги, а не вершины) */
                    // TODO: передавать карту города
                    String[] args = new String[] { "tl_4", "tl_2"};
                    getContainerController()
                            .createNewAgent("car_0", "com.company.Agent_Car", args)
                            .start();
                    getContainerController()
                            .createNewAgent("car_1", "com.company.Agent_Car", args)
                            .start();

                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

        } );
    }
}
