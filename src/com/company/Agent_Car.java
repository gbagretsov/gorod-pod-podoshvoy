package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;


public class Agent_Car extends Agent {

    private String currentTrafficLight;
    private String finish;

    @Override
    protected void setup() {
        /* Запоминаем позицию финиша */
        finish = "tl_0";

        /* Въезжаем в город и сообщаем об этом светофору, стоящему в начале исходной дуги */
        Object[] args = getArguments();
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(args[0].toString(), AID.ISLOCALNAME));
        message.setOntology("coming-to-town");
        message.setContent(args[1].toString());
        send(message);
        currentTrafficLight = args[1].toString();
        /* Debug output */
        System.out.println("Debug: car " + getLocalName() + " added");
    }

}
