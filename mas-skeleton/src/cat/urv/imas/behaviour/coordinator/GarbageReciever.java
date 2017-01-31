/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import java.util.ArrayList;

/**
 *
 * @author Emanuel
 */
public class GarbageReciever extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behavior
     * @param mt Template to receive future responses in this conversation
     */
    public GarbageReciever(CoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting REQUESTs from authorized agents");
    }

    /**
     * When System Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     * 
     * @param msg message received.
     * @return AGREE message when all was OK, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResponse(ACLMessage msg) {
        CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        try {
            MessageWrapper msgWrapper = (MessageWrapper) msg.getContentObject();
            if (msgWrapper != null) {
                switch (msgWrapper.getType()) {
                    case MessageContent.GARBAGE_FOUND:
                        ArrayList<BuildingCell> garbageBuildings = (ArrayList<BuildingCell>) msgWrapper.getObject();
                        agent.addGarbageFound(garbageBuildings);
                        agent.log("Recieved from Scout Coordinator " + garbageBuildings);
                        agent.addBehaviour(new RequesterBehaviour(agent, agent.createStepFinished()));
                        
                        reply.setPerformative(ACLMessage.AGREE);
                        break;
                    case MessageContent.NEW_STEP:
                        agent.log("Recieved from System agent new simulation step");
                        agent.addBehaviour(new RequesterBehaviour(agent, agent.createNewStepRequest()));                        
                        reply.setPerformative(ACLMessage.AGREE);
                        break;
                }
            }          
        } catch (UnreadableException e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.getMessage());
        }
        
        agent.log("Response being prepared");
        return reply;
    }

    /**
     * After sending an AGREE message on prepareResponse(), this behaviour
     * sends an INFORM message with the whole game settings.
     * 
     * NOTE: This method is called after the response has been sent and only when one
     * of the following two cases arise: the response was an agree message OR no
     * response message was sent. 
     *
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        return reply;
    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}
