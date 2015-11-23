package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;


public class Agent_Car extends Agent {

    private String currentTrafficLight;
    private String finish;
    private ArrayList<String> path;
    private  Object[] args;
    private Date startTime;

    @Override
    protected void setup() {
        /* Запоминаем позицию финиша */
        finish = "tl_0";

        /* Въезжаем в город и сообщаем об этом светофору, стоящему в конце исходной дуги */
        args = getArguments();
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(args[1].toString(), AID.ISLOCALNAME));
        message.setOntology("coming-to-town");
        message.setContent(args[0].toString());
        send(message);
        currentTrafficLight = args[1].toString();

        /* Инициализация маршрута */
        path = new ArrayList<String>();
        path.add(currentTrafficLight);

        /* Debug output */
        System.out.println("Debug: car " + getLocalName() + " added");

        /* Запоминаем текущее время */
        startTime = new Date();

        addBehaviour(new TrafficLightConversation());
    }

    private class TrafficLightConversation extends CyclicBehaviour {

        @Override
        public void action() {
            /* Ждём зелёного сигнала от светофора */
            MessageTemplate template = MessageTemplate.MatchOntology("green-light");
            ACLMessage response = myAgent.receive(template);
            if (response == null) {
                block();
            }
            else {
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(new AID(response.getSender().getLocalName(), AID.ISLOCALNAME));
                message.setOntology("green-light");
                /* Если мы на финише, сообщаем об этом и заканчиваем работу */
                if (response.getSender().getLocalName().equals(finish)) {
                    message.setContent("finish");
                    send(message);
                    myAgent.doDelete();
                }
                else {
                    message.setContent("proceed");
                    send(message);

                    /* Ждём предложений от светофоров */
                    template = MessageTemplate.MatchOntology("traffic-lights-contract");
                    /* В данном случае допустимо использовать blockingReceive(), т.к. поведение единственное */
                    response = myAgent.blockingReceive(template);

                    /* Выбираем путь */
                    String decision = choosePath(parseTLProposals(response.getContent()));

                    /* Меняем положение */
                    String old = currentTrafficLight;
                    currentTrafficLight = decision;
                    System.out.println("Debug: car " + getAgent().getLocalName()
                            + " moves from " + old + " to " + currentTrafficLight);

                    /* Добавляем светофор в маршрут */
                    path.add(decision);

                    /* Отправляем ответ */
                    ACLMessage chosenOption = new ACLMessage(ACLMessage.AGREE);
                    chosenOption.setOntology("traffic-lights-contract");
                    chosenOption.setContent(decision);
                    chosenOption.addReceiver(new AID(response.getSender().getLocalName(), AID.ISLOCALNAME));
                    send(chosenOption);
                }
            }
        }

        private String choosePath(Hashtable<String, Integer> proposals) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

          /*String[] options = proposals.keySet().toArray(new String[0]);
            int quantity = options.length;
            Random rand = new Random();
            return options[rand.nextInt(quantity)]; */
            return Algorythm.GetNextTL(currentTrafficLight, ((int[][]) ((Agent_Car) myAgent).args[2]), proposals);
        }

        private Hashtable<String, Integer> parseTLProposals(String response) {
            /* Формат сообщения: "имя_светофора1:число1;имя_светофора2:число2;...;имя_светофораN:числоN" */
            Hashtable<String, Integer> proposals = new Hashtable<String, Integer>();
            String[] keyValuePairs = response.split(";");
            for (String pair: keyValuePairs) {
                String[] keyValue = pair.split(":");
                proposals.put(keyValue[0], Integer.parseInt(keyValue[1]));
            }
            return proposals;
        }
    }

    @Override
    protected void takeDown() {
        /* По прибытии выводим весь маршрут и время поездки на печать */
        String route = "";
        if (currentTrafficLight.equals(finish)) {
            for (String s : path) {
                route = route.concat(s);
                if (!s.equals(finish)) {
                    route = route.concat(" -> ");
                }
            }
        }
        Date arrivalTime = new Date();
        long tripDuration = arrivalTime.getTime() - startTime.getTime();
        System.out.println("Car " + getLocalName() +
                " arrived to its destination " + finish +
                " in " + tripDuration / 1000 + " seconds by route: " + route);
    }
}
