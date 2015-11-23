package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;


// TODO: избавиться от blockingReceive()
public class Agent_Car extends Agent {

    private String currentTrafficLight;
    private String finish;
    private ArrayList<String> path;

    // TODO: хранить время в пути

    @Override
    protected void setup() {
        /* Запоминаем позицию финиша */
        finish = "tl_0";

        /* Въезжаем в город и сообщаем об этом светофору, стоящему в конце исходной дуги */
        Object[] args = getArguments();
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
            // TODO: изменить алгоритм выбора
            // TODO: добавить задержку на две секунды (скорее всего, именно здесь, т.к. алгоритм должен быть автономным)
            String[] options = proposals.keySet().toArray(new String[0]);
            int quantity = options.length;
            Random rand = new Random();
            return options[rand.nextInt(quantity)];
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
        /* По прибытии выводим весь маршрут на печать */
        if (currentTrafficLight.equals(finish)) {
            System.out.print("Car " + getLocalName() + " arrived to its destination " + finish + " by route: ");
            for (String s : path) {
                System.out.print(s);
                if (!s.equals(finish)) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();
        }
    }
}
