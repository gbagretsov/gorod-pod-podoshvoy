package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;

import java.util.*;


public class Agent_TrafficLight extends Agent {

    private enum CarsHandlerState {
        SENDING_GREEN_LIGHT_MESSAGE,
        WAITING_FOR_GREEN_LIGHT_MESSAGE_RESPONSE,
        INITIATING_CONTRACTS_NET,
        HANDLING_PROPOSALS,
        WAITING_FOR_PROPOSALS_MESSAGE_RESPONSE
    }

    private Hashtable<String, LinkedList<String>> cars;
    private List<String> outcoming;
    private String currentQueue;

    // Шаблон входящих сообщений на запрос услуги
    MessageTemplate cfpTemplate = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            MessageTemplate.MatchOntology("traffic-lights-contract"));

    @Override
    protected void setup() {

        Object[] args = getArguments();
        int[][] map = (int[][]) args[0];
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
        System.out.println("Debug: " + getAID().getLocalName()
                + "; in: "  + incomingString
                + "; out: " + outcomingString);

        // TODO: изменить длительность цикла светофора (или изменить логику работы в целом)
        addBehaviour(new QueueSwitchBehaviour(this, 2000));
        addBehaviour(new NewCarsComingToTownHandlerBehaviour());
        addBehaviour(new CarsHandlerBehaviour());
        addBehaviour(new TLRequestsHandler(this, cfpTemplate));
    }

    private class NewCarsComingToTownHandlerBehaviour extends CyclicBehaviour {

        /* Только что созданная машина сама сообщает светофору, что она въезжает в город.
         * В этом поведении происходит обработка таких сообщений */

        MessageTemplate newCarTemplate = MessageTemplate.MatchOntology("coming-to-town");

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

    private class CarsHandlerBehaviour extends CyclicBehaviour {

        /* Обработка машин в очереди.
         * Текущая очередь записана в поле currentQueue и меняется в другом поведении.
         * В сети контрактов данный светофор по сути является посредником между машиной и другими светофорами
         * (т. к. в нашей модели машина имеет доступ только к светофору, на котором она находится)*/

        String currentCar;
        CarsHandlerState currentState = CarsHandlerState.SENDING_GREEN_LIGHT_MESSAGE;

        @Override
        public void action() {

            if (currentState == CarsHandlerState.SENDING_GREEN_LIGHT_MESSAGE) {

                /* Выбираем очередную машину */
                if (currentQueue == null) {
                    return;
                }
                currentCar = ((Agent_TrafficLight) myAgent).pollCarFromQueue(currentQueue);
                if (currentCar == null) {
                    return;
                }

                /* Посылаем сообщение машине - "зелёный свет" */
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.addReceiver(new AID(currentCar, AID.ISLOCALNAME));
                message.setOntology("green-light");
                message.setContent("green-light");
                send(message);

                currentState = CarsHandlerState.WAITING_FOR_GREEN_LIGHT_MESSAGE_RESPONSE;

            } else if (currentState == CarsHandlerState.WAITING_FOR_GREEN_LIGHT_MESSAGE_RESPONSE) {

                /* Получаем ответ. Если машина приехала в пункт назначения, заканчиваем итерацию */
                MessageTemplate responseTemplate = MessageTemplate.MatchOntology("green-light");
                ACLMessage response = myAgent.receive(responseTemplate);
                if (response == null) {
                    block();
                } else if (response.getContent().equals("finish")) {
                    currentCar = null;
                    currentState = CarsHandlerState.SENDING_GREEN_LIGHT_MESSAGE;
                    return;
                } else {
                    currentState = CarsHandlerState.INITIATING_CONTRACTS_NET;
                }

            } else if (currentState == CarsHandlerState.INITIATING_CONTRACTS_NET) {

                /* Если машине нужно ехать дальше, запускаем сеть контрактов */
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                for (String tl : outcoming) {
                    msg.addReceiver(new AID(tl, AID.ISLOCALNAME));
                }
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                msg.setOntology("traffic-lights-contract");
                /* Мы хотим получить ответ в течение секунды */
                // TODO: уточнить срок получения ответа в сети контрактов
                msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
                msg.setContent("queue-length");

                currentState = CarsHandlerState.HANDLING_PROPOSALS;

                addBehaviour(new ContractNetInitiator(getAgent(), msg) {

                    @Override
                    protected void handleAllResponses(final Vector responses, final Vector acceptances) {

                        /* В этом методе обрабатываются предложения других светофоров */

                        if (currentState == CarsHandlerState.HANDLING_PROPOSALS) {
                            String content = "";

                            /* Составляем ответное сообщение для машины.
                             * Формат сообщения: "имя_светофора1:число1;имя_светофора2:число2;...;имя_светофораN:числоN" */
                            Enumeration proposals = responses.elements();
                            while (proposals.hasMoreElements()) {
                                ACLMessage msg = (ACLMessage) proposals.nextElement();
                                content = content.concat(msg.getSender().getLocalName()).concat(":")
                                        .concat(msg.getContent()).concat(";");
                            }
                            content = content.substring(0, content.length() - 1);

                            /* Отправляем сообщение машине */
                            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                            message.addReceiver(new AID(currentCar, AID.ISLOCALNAME));
                            message.setOntology("traffic-lights-contract");
                            message.setContent(content);
                            send(message);

                            currentState = CarsHandlerState.WAITING_FOR_PROPOSALS_MESSAGE_RESPONSE;
                        };

                        if (currentState == CarsHandlerState.WAITING_FOR_PROPOSALS_MESSAGE_RESPONSE) {

                            /* Получаем ответ - решение машины */
                            MessageTemplate responseTemplate = MessageTemplate.and(
                                    MessageTemplate.MatchOntology("traffic-lights-contract"),
                                    MessageTemplate.MatchPerformative(ACLMessage.AGREE));

                            ACLMessage msg = myAgent.receive(responseTemplate);
                            while (msg == null) {
                                block();
                                msg = myAgent.receive(responseTemplate);
                            }

                            /* Создаём ответы светофорам - и для подтверждения, и для отказа от услуги */
                            String chosen = msg.getContent();
                            Enumeration e = responses.elements();
                            while (e.hasMoreElements()) {
                                ACLMessage offer = (ACLMessage) e.nextElement();
                                if (offer.getPerformative() == ACLMessage.PROPOSE) {
                                    ACLMessage reply = offer.createReply();
                                    if (chosen != null && offer.getSender().getLocalName().equals(chosen)) {
                                        /* В сообщении для выбранного светофора указываем имя машины */
                                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                        reply.setContent(currentCar);
                                    } else {
                                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                    }
                                    acceptances.addElement(reply);
                                }
                            } /* end of while block */

                            currentCar = null;
                            currentState = CarsHandlerState.SENDING_GREEN_LIGHT_MESSAGE;
                        } /* end of (currentState == CarsHandlerState.WAITING_FOR_PROPOSALS_MESSAGE_RESPONSE) */
                    } /* end of handleAllResponses() */
                }); /* end of addBehaviour() */
            } /* end of (currentState == CarsHandlerState.INITIATING_CONTRACTS_NET) */
        } /* end of action() */
    } /* end of CarsHandlerBehaviour */

    private class TLRequestsHandler extends ContractNetResponder {
        public TLRequestsHandler(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) {
            /* Обработка запроса услуги
             * В своём предложении светофор записывает длину очереди */
            int proposal = ((Agent_TrafficLight) myAgent).getQueueLength(cfp.getSender().getLocalName());
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent(String.valueOf(proposal));
            return propose;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept){
            /* Если предложение светофора приняли, он добавляет машину в очередь */
            String incomingCar = accept.getContent();
            String from = accept.getSender().getLocalName();
            ((Agent_TrafficLight) myAgent).putCarToQueue(from, incomingCar);
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }
    }

    // TODO: изменить алгоритм переключения
    private class QueueSwitchBehaviour extends TickerBehaviour {

        /* queuesList - список имён светофоров
         * i - индекс текущего светофора (т. е. часть имени без "tl_") */
        String[] queuesList = cars.keySet().toArray(new String[0]);
        int i = -1;

        /* period - интервал в миллисекундах, через который циклически будет выполняться метод onTick() */
        public QueueSwitchBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            /* Выбираем следующий светофор; если дошли до конца списка - начинаем сначала */
            if (++i >= queuesList.length) {
                i = 0;
            }
            currentQueue = queuesList[i];
            System.out.println("Debug: " + myAgent.getLocalName() + " changed to "
                    + currentQueue + " handling at " + new Date());
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
