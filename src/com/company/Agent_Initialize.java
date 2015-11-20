package com.company;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.wrapper.StaleProxyException;

public class Agent_Initialize extends Agent {

    @Override
    protected void setup() {
        addBehaviour(new WakerBehaviour(this, 5000) {
            @Override
            protected void handleElapsedTimeout() {
                try {
                    for (int i = 0; i < 10; i++) {
                        getContainerController()
                                .createNewAgent("test".concat(String.valueOf(i)), "com.company.Agent_Test", null)
                                .start();
                    }
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
                this.getAgent().doDelete();
            }
        } );
    }
}
