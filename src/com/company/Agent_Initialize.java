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
                    // TODO: написать генерацию входных вершин для авто
                    Object[] args = new Object[] { "tl_4", "tl_2", city.getMap()};
                    Object[] args1 = new Object[] { "tl_3", "tl_4", city.getMap()};

                    getContainerController()
                            .createNewAgent("car_0", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_1", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_2", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_3", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_4", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_5", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_6", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_7", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_8", "com.company.Agent_Car", args1)
                            .start();
                    getContainerController()
                            .createNewAgent("car_9", "com.company.Agent_Car", args1)
                            .start();
                    Algorythm.cars_created += 10;
                    //Algorythm.cars_created ++;

                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

        } );
    }
}
