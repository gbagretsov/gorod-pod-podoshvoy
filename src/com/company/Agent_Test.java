package com.company;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Agent_Test extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello! Name's " + this.getAID().getLocalName());
        Object[] args = this.getArguments();
        if (args != null && args.length != 0) {
            for (Object o : args) {
                System.out.println(o);
            }
        }
        else {
            System.out.println("I haven't been passed any arguments :( ");
        }
        this.addBehaviour(new TestOneShotBehaviour());
    }

    private class TestOneShotBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Testing one-shot behaviour on " + this.getAgent().getAID().getLocalName() + "...");
        }
    }
}
