package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.util.*;


public class Agent_Car extends Agent {

    private Hashtable<String, Boolean>  TL_Visited;
    private String currentTrafficLight;
    private String finish;
    private ArrayList<String> path;
    private ArrayList<String> cant_go_here;
    private  Object[] args;
    private Date startTime;
    private Integer[][] map;
    private Integer[][] value_memory;
    private Queue<Integer> key_memory;

    SequentialBehaviour sequentialBehaviour;
    ReceiverBehaviour.Handle receiverHandler;
    ReceiverBehaviour greenLightReceiver;

    MessageTemplate greenLightTemplate = MessageTemplate.MatchOntology("green-light");

    @Override
    protected void setup()  {

        /* Увеличиваем счётчик для статистики */
        CITY.increaseCarsAmount();

        /* Въезжаем в город и сообщаем об этом светофору, стоящему в конце исходной дуги */
        args = getArguments();
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(args[1].toString(), AID.ISLOCALNAME));
        message.setConversationId("NEW".concat(String.valueOf(CITY.getNextID())));
        message.setOntology("coming-to-town");
        message.setContent(args[0].toString());
        send(message);
        currentTrafficLight = args[1].toString();
        map = (Integer[][]) args[2];
        value_memory = (Integer[][]) args[2];
        key_memory = new PriorityQueue<Integer>();

        /* Запоминаем позицию финиша */
        finish = args[3].toString();

        /* Инициализация маршрута */
        path = new ArrayList<String>();
        path.add(currentTrafficLight);

        /* Debug output */
        /*System.out.println("Debug: car " + getLocalName() + " added");*/

        /* Запоминаем текущее время */
        startTime = new Date();

        /* Подготовка и запуск последовательного поведения, состоящего из:
           1. получения сообщения;
           2. логики работы на перекрёстке */
        receiverHandler = ReceiverBehaviour.newHandle();
        greenLightReceiver = new ReceiverBehaviour(this, receiverHandler, -1, greenLightTemplate);
        sequentialBehaviour = new SequentialBehaviour(this);
        sequentialBehaviour.addSubBehaviour(greenLightReceiver);
        sequentialBehaviour.addSubBehaviour(new RoadsCrossHandler());
        addBehaviour(sequentialBehaviour);

        /*инициализируем список вершин, запрещенных для проезда*/
        cant_go_here = new ArrayList<String> ();

    }

    private class RoadsCrossHandler extends OneShotBehaviour {

        @Override
        public void action() {
            /* Ждём зелёного сигнала от светофора */
            ACLMessage response = null;
            try {
                response = receiverHandler.getMessage();
                ACLMessage reply = response.createReply();
                reply.setContent("proceed");
                /* Если мы на финише, сообщаем об этом и заканчиваем работу */
                if (response.getSender().getLocalName().equals(finish)) {
                    send(reply);
                    myAgent.doDelete();
                }
                else {
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (String tl : getOutcomingTrafficLights()) {
                        cfp.addReceiver(new AID(tl, AID.ISLOCALNAME));
                    }
                    cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    cfp.setConversationId("CN".concat(String.valueOf(CITY.getNextID())));
                    cfp.setOntology("traffic-lights-contract");
                    /* Мы хотим получить ответ в течение секунды */
                    // TODO: пока пришлось отключить тайм-аут, т.к. часто светофоры не успевают принять сообщение
                    //cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
                    cfp.setContent(currentTrafficLight);

                    /* Запускаем контрактную сеть */
                    addBehaviour(new TLContractNetInitiator(myAgent, cfp, reply));
                }
            } catch (ReceiverBehaviour.TimedOut timedOut) {
                timedOut.printStackTrace();
            } catch (ReceiverBehaviour.NotYetReady notYetReady) {
                notYetReady.printStackTrace();
            }
        }

        private ArrayList<String> getOutcomingTrafficLights() {
            int index = Integer.parseInt(currentTrafficLight.replace("tl_", ""));
            ArrayList<String> outcoming = new ArrayList<String>();
            for (int i = 0; i < map.length; i++) {
                if (map[index][i] == 1) {
                    outcoming.add("tl_".concat(String.valueOf(i)));
                }
            }
            return outcoming;
        }

    }

    private class TLContractNetInitiator extends ContractNetInitiator {

        String decision;
        ACLMessage greenLightReply;

        public TLContractNetInitiator(Agent a, ACLMessage cfp, ACLMessage greenLightReply) {
            super(a, cfp);
            this.greenLightReply = greenLightReply;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            Hashtable<String, Integer> proposalsHashtable = new Hashtable<String, Integer>();
            Enumeration proposals = responses.elements();
            while (proposals.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) proposals.nextElement();
                proposalsHashtable.put(msg.getSender().getLocalName(), Integer.parseInt(msg.getContent()));
            }

            // TODO: обрабатывать сломанные (не ответившие) светофоры
            // TODO: возможно, стоит по-другому обрабатывать ситуации, когда некуда поворачивать
            if (proposalsHashtable.isEmpty()) {
                System.out.println("Warning: " + myAgent.getLocalName() + " has no roads to turn and is now terminating...");
                myAgent.doDelete();
                return;
            }

            /* Выбираем путь */
            //System.out.println(myAgent.getLocalName() + proposalsHashtable.toString() + currentTrafficLight);
            decision = choosePath(proposalsHashtable);

            /* Имитируем поворот */
            //if (path.size()>1)
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* Создаём ответы светофорам - и для подтверждения, и для отказа от услуги */
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage offer = (ACLMessage) e.nextElement();
                if (offer.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = offer.createReply();
                    if (decision != null && offer.getSender().getLocalName().equals(decision)) {
                        /* В сообщении для выбранного светофора указываем имя исходного светофора */
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent(currentTrafficLight);
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    acceptances.addElement(reply);
                }
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            /* Меняем положение */
            String old = currentTrafficLight;
            currentTrafficLight = decision;
            /*System.out.println("Debug: car " + getAgent().getLocalName()
                + " moves from " + old + " to " + currentTrafficLight);*/

            /* Добавляем светофор в маршрут */
            path.add(decision);
            /* Обновляем список запрещенных вершин */

            /* Отправляем ответ светофору, с которого свернули */
            send(greenLightReply);
            /* Отправляем данные для статистики */
            CITY.increaseRoadsPassed();
            /* Вновь входим в режим ожидания сообщения, но уже от нового светофора */
            greenLightReceiver.reset();
            sequentialBehaviour.reset();
            myAgent.addBehaviour(sequentialBehaviour);
        }

        private String choosePath(Hashtable<String, Integer> proposals) {
            Integer i = Integer.valueOf(finish.replace("tl_", ""));
            ArrayList<String> cant = Algorythm.CantGoThere(currentTrafficLight, ((Integer[][]) ((Agent_Car) myAgent).args[2]), path, i );
            return Algorythm.GetNextTL(currentTrafficLight, ((Integer[][]) ((Agent_Car) myAgent).args[2]), proposals, path, cant, i, key_memory, value_memory );
            /*Object[] props = proposals.keySet().toArray();
            return props[new Random().nextInt(props.length)].toString();*/
        }

    }

    @Override
    protected void takeDown() {
        /* По прибытии выводим весь маршрут и время поездки на печать, а также отправляем данные для статистики */
        String route = "";
        if (currentTrafficLight.equals(finish)) {
            if (!path.contains(finish)) {
                path.add(finish);
            }
            for (String s : path) {
                route = route.concat(s);
                if (!s.equals(finish)) {
                    route = route.concat(" -> ");
                }
            }
            Date arrivalTime = new Date();
            long tripDuration = arrivalTime.getTime() - startTime.getTime();
            System.out.println("Car " + getLocalName() +
                    " arrived to its destination " + finish +
                    " in " + (double) tripDuration / 1000 + " seconds by route: " + route);

            CITY.handleCarFinish(this.getLocalName(), tripDuration, path.size());
        }
        /* Если машина сломалась и не доехала до финиша, отправляем данные о поломке для статистики */
        else {
            CITY.handleCarFailure();
        }
    }
}
