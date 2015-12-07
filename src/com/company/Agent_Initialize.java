package com.company;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.Random;

public class Agent_Initialize extends Agent {

    /* Значения ontology */
    public static final String QUEUE_LENGTH = "queue-length";
    public static final String LOCATION = "location";

    /* Значения content */
    public static final String GREEN_LIGHT = "green-light";
    public static final String PROCEED = "proceed";

    @Override
    protected void setup() {
        addBehaviour(new OneShotBehaviour(this) {

            CITY city;

            @Override
            public void action() {
                /* Создаём город */
                try {
                    city = new CITY();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Integer[][] map = city.getMap();

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
                     * Аргументы: { beginFrom, beginTo, map, finish } */

                    /* Случайная генерация */
                    /*for (int i = 0; i < 10; i++) {
                        Thread.sleep(500);
                        Object[] args = new Object[4];
                        Random random = new Random();
                        int from, to, finish;
                        do {
                            from = random.nextInt(city.getMap().length);
                            to = random.nextInt(city.getMap().length);
                            finish = random.nextInt(city.getMap().length);
                        } while (map[from][to] != 1 || to == 0 || finish == to);
                        args[0] = "tl_".concat(String.valueOf(from));
                        args[1] = "tl_".concat(String.valueOf(to));
                        args[2] = city.getMap();
                        args[3] = "tl_".concat(String.valueOf(finish));
                        getContainerController()
                                .createNewAgent("car_".concat(String.valueOf(i)), "com.company.Agent_Car", args)
                                .start();
                    }*/

                    /* Тестовая генерация */
                    Thread.sleep(10000);
                    for (int i = 0; i < 30; i++) {
                        Thread.sleep(500);
                        Object[] args = new Object[4];
                        args[0] = "tl_0";
                        args[1] = "tl_1";
                        args[2] = city.getMap();
                        args[3] = "tl_19";
                        getContainerController()
                                .createNewAgent("car_".concat(String.valueOf(i)), "com.company.Agent_Car", args)
                                .start();
                    }

                } catch (StaleProxyException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        } );
    }
}
