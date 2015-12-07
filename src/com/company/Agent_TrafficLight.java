package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.*;

// TODO: судя по дебаггеру, агент слишком долго сидит в поведениях QueueSwitch и Sequential
public class Agent_TrafficLight extends Agent {

    private Hashtable<String, LinkedList<String>> cars;
    private List<String> outcoming;
    private String currentQueue;
    private String currentCar;

    SequentialBehaviour sequentialBehaviour;
    ReceiverBehaviour.Handle receiverHandler;
    ReceiverBehaviour greenLightReceiver;

    /* Шаблоны входящих сообщений:
     * 1. запрос услуги
     * 2. ответ на сообщение "зелёный свет"
     * 3. сообщение о новой машине */
    MessageTemplate cfpTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            MessageTemplate.MatchOntology("traffic-lights-contract"));
    MessageTemplate greenLightTemplate = MessageTemplate.MatchOntology("green-light");
    MessageTemplate newCarTemplate = MessageTemplate.MatchOntology("coming-to-town");

    @Override
    protected void setup() {

        Object[] args = getArguments();
        Integer[][] map = (Integer[][]) args[0];
        int index = Integer.parseInt(args[1].toString());

        /* Создаём очереди входящих */
        cars = new Hashtable<String, LinkedList<String>>();
        for (int i = 0; i < map.length; i++) {
            if (map[i][index] == 1) {
                cars.put("tl_".concat(String.valueOf(i)), new LinkedList<String>());
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
        /*System.out.println("Debug: " + getAID().getLocalName()
                + "; in: "  + incomingString
                + "; out: " + outcomingString);*/

        if (cars.keySet().size() > 0) {

            addBehaviour(new QueueSwitchBehaviour(this, 1000));
            addBehaviour(new NewCarsComingToTownHandlerBehaviour());
            addBehaviour(new CarRequestsHandler(this, cfpTemplate));

            /* Инициализация и запуск последовательных поведений, связанных с обработкой машин в очереди */
            receiverHandler = ReceiverBehaviour.newHandle();
            sequentialBehaviour = new SequentialBehaviour(this);
            sequentialBehaviour.addSubBehaviour(new GreenLightSender());
            addBehaviour(sequentialBehaviour);
        }
    }

    private class NewCarsComingToTownHandlerBehaviour extends CyclicBehaviour {

        /* Только что созданная машина сообщает светофору, что она въезжает в город.
         * В этом поведении происходит обработка таких сообщений */

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive(newCarTemplate);
            if (msg != null) {
                String car = msg.getSender().getLocalName();
                String from = msg.getContent();
                ((Agent_TrafficLight) getAgent()).putCarToQueue(from, car);
                /* Debug */
                System.out.println(
                        "Debug: new car " + car + " comes to town, starting point is between " +
                        from + " and " + getAgent().getLocalName());
            }
            else {
                block();
            }
        }
    };

    private class GreenLightSender extends CyclicBehaviour {

        /* Обработка машин в очереди.
         * Текущая очередь записана в поле currentQueue и меняется в другом поведении. */

        @Override
        public void action() {
            /* Выбираем очередную машину */
            if (currentQueue == null) {
                block(500);
                return;
            }
            currentCar = ((Agent_TrafficLight) myAgent).pollCarFromQueue(currentQueue);
            if (currentCar == null) {
                block(100);
                return;
            }

            /* Отправляем данные для сбора статистики */
            int load = 0;
            for (LinkedList<String> s : cars.values()) {
                load += s.size();
            }
            CITY.setMaxLoadIfNeeded(load, myAgent.getLocalName());

            /* Посылаем сообщение машине - "зелёный свет" */
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setConversationId("GL".concat(String.valueOf(CITY.getNextID())));
            message.addReceiver(new AID(currentCar, AID.ISLOCALNAME));
            message.setOntology("green-light");
            message.setContent("green-light");
            message.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            send(message);

            /* Сохраняем имя текущей машины в шаблон */
            greenLightTemplate = MessageTemplate.and(
                    MessageTemplate.MatchOntology("green-light"),
                    MessageTemplate.MatchSender(new AID(currentCar, AID.ISLOCALNAME)));
            greenLightReceiver = new ReceiverBehaviour(myAgent, receiverHandler, 10000, greenLightTemplate);
            sequentialBehaviour.addSubBehaviour(greenLightReceiver);
            sequentialBehaviour.addSubBehaviour(new GreenLightResponseHandler());
            sequentialBehaviour.removeSubBehaviour(this);
        }
    }

    private class GreenLightResponseHandler extends OneShotBehaviour {

        /* Обработка ответа на "зелёный свет" */

        @Override
        public void action() {
            /* Получаем ответ. После ответа можно переходить к следующей машине */
            ACLMessage response = null;
            try {
                response = receiverHandler.getMessage();
            } catch (ReceiverBehaviour.TimedOut timedOut) {
                /* Если машина не ответила, выводим предупреждение и переходим к следующей */
                System.out.println("Warning: " + myAgent.getLocalName() + " didn't get reply from " + currentCar);
            } catch (ReceiverBehaviour.NotYetReady notYetReady) {
                notYetReady.printStackTrace();
            } finally {
                currentCar = null;

                /* Запускаем обработку машин заново */
                sequentialBehaviour.removeSubBehaviour(greenLightReceiver);
                sequentialBehaviour = new SequentialBehaviour(myAgent);
                sequentialBehaviour.addSubBehaviour(new GreenLightSender());
                addBehaviour(sequentialBehaviour);
            }
        }
    }

    private class CarRequestsHandler extends ContractNetResponder {
        public CarRequestsHandler(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        // TODO: обработчик слишком поздно получает собщение
        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {
            /* Обработка запроса услуги
             * В своём предложении светофор записывает длину очереди */
            int proposal = ((Agent_TrafficLight) myAgent).getQueueLength(cfp.getContent());
            /* !!! DEBUG INFO !!! */
            /*long delta = System.currentTimeMillis() - cfp.getReplyByDate().getTime();
            if (delta > 0) {
                System.out.println
                        ("OH CRAP!!! " + myAgent.getLocalName() + " is late for " + String.valueOf((double) delta / 1000) + " seconds!");
            }*/
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent(String.valueOf(proposal));
            return propose;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept){
            /* Если предложение светофора приняли, он добавляет машину в очередь */
            String incomingCar = accept.getSender().getLocalName();
            String from = accept.getContent();
            ((Agent_TrafficLight) myAgent).putCarToQueue(from, incomingCar);
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
    }

    private class QueueSwitchBehaviour extends TickerBehaviour {

        /* queuesList - список имён светофоров
         * i - индекс текущего светофора (т. е. часть имени без "tl_")
         * iterationsPassedSinceLastSwitch - время последнего переключения светофора */
        String[] queuesList = cars.keySet().toArray(new String[0]);
        int i = 0;

        /* period - интервал в миллисекундах, через который циклически будет выполняться метод onTick() */
        public QueueSwitchBehaviour(Agent a, long period) {
            super(a, period);
            /*System.out.println("Debug: current time is " + new Date());*/
        }

        @Override
        protected void onTick() {
            if (currentQueue == null) {
                currentQueue = queuesList[0];
                return;
            }

            /* Если в очереди нет машин ИЛИ переключались более 30 секунд назад */
            if (getQueueLength(currentQueue) == 0 || this.getTickCount() >= 30) {
                /* Выбираем следующий светофор; если дошли до конца списка - начинаем сначала */
                if (++i >= queuesList.length) {
                    i = 0;
                }
                currentQueue = queuesList[i];
                /*System.out.println("Debug: " + myAgent.getLocalName() + " changed to "
                        + currentQueue + " handling at " + new Date());*/

                /* Сбрасываем счётчик итераций */
                reset();
            }
        }
    }

    /* Получить длину очереди от светофора tlLocalName */
    private int getQueueLength(String tlLocalName) {
        return cars.get(tlLocalName).size();
    }

    /* Получить имя первой машины в очереди tlLocalName
     * При этом машина покидает очередь */
    private String pollCarFromQueue(String tlLocalName) {
        return cars.get(tlLocalName).pollFirst();
    }

    /* Поместить машину carLocalName в очередь от светофора tlLocalNeme */
    private void putCarToQueue(String tlLocalName, String carLocalName) {
        cars.get(tlLocalName).addLast(carLocalName);
    }

}
