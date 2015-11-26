package com.company;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
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
                     * Аргументы: { beginFrom, beginTo } (машина начинает движение с дуги, а не вершины) */
                    // TODO: написать генерацию входных вершин для авто

                    Object[] args__0 = new Object[] { "tl_6", "tl_7", city.getMap()};
                    Object[] args__1 = new Object[] { "tl_6", "tl_7", city.getMap()};
                    Object[] args__2 = new Object[] { "tl_25", "tl_10", city.getMap()};
                    Object[] args__3 = new Object[] { "tl_24", "tl_23", city.getMap()};
                    Object[] args__4 = new Object[] { "tl_24", "tl_25", city.getMap()};

                    Integer current_args = 0;
                    for(Integer i = 0; i< 200; i++) {
                        if (current_args == 0) {
                            getContainerController()
                                    .createNewAgent("car_".concat(i.toString()), "com.company.Agent_Car", args__0 )
                                    .start();
                        current_args++;
                        }
                        else if (current_args == 1) {
                            getContainerController()
                                    .createNewAgent("car_".concat(i.toString()), "com.company.Agent_Car", args__1 )
                                    .start();
                             current_args++;
                        }
                        else if (current_args == 2) {
                            getContainerController()
                                    .createNewAgent("car_".concat(i.toString()), "com.company.Agent_Car", args__2 )
                                    .start();
                            current_args++;
                        }
                        else if (current_args == 3) {
                            getContainerController()
                                    .createNewAgent("car_".concat(i.toString()), "com.company.Agent_Car", args__3 )
                                    .start();
                            current_args++;
                        }
                        else if (current_args == 4) {
                            getContainerController()
                                    .createNewAgent("car_".concat(i.toString()), "com.company.Agent_Car", args__4 )
                                    .start();
                            current_args++;
                        }
                        else if (current_args == 5) {current_args = 0;}
                    }
                    Algorythm.cars_created += 200;
                    //Algorythm.cars_created ++;

                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }

        } );
    }
}
