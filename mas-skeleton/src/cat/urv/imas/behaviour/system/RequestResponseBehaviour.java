/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.behaviour.system;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.UnreadableException;
import java.io.IOException;

/**
 * A request-responder behavior for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class RequestResponseBehaviour extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behavior
     * @param mt Template to receive future responses in this conversation
     */
    public RequestResponseBehaviour(SystemAgent agent, MessageTemplate mt) {
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
        SystemAgent agent = (SystemAgent)this.getAgent();
        ACLMessage reply = msg.createReply();    
        
        try {
            MessageWrapper wrapper = (MessageWrapper) msg.getContentObject();
            if (wrapper != null) {
                switch (wrapper.getType()) {
                    case MessageContent.GET_MAP:
                        agent.log("Request received: " + MessageContent.GET_MAP);
                        reply.setPerformative(ACLMessage.AGREE);                        
                        break;
                    case MessageContent.STEP_FINISHED:
                        // update list of agents
                        agent.getGame().updateAgentList();

                        agent.log("Notification recieved " + MessageContent.STEP_FINISHED);
                        // no need to answer
                        reply = null;
                        int step = agent.getGame().getCurrentSimulationSteps();

                        // TODO: falta harvester finish!
                        
                        agent.log("Step: " + step + " finished");
                        
                        if (step < agent.getGame().getSimulationSteps()) {
                            agent.getGame().setCurrentSimulationSteps(step + 1);
                            
                            SequentialBehaviour runStep = new SequentialBehaviour(agent);
                            runStep.addSubBehaviour(new NewGarbageBehaviour(agent));
                            runStep.addSubBehaviour(new SendNewStep(agent, agent.createNewStep()));
                            agent.addBehaviour(runStep);
                        }
                        
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

        SystemAgent agent = (SystemAgent)this.getAgent();
        ACLMessage reply = msg.createReply();    
        
        try {
            MessageWrapper msgWrapper = (MessageWrapper) msg.getContentObject();
            if (msgWrapper != null) {
                switch (msgWrapper.getType()) {
                    case MessageContent.GET_MAP:
                        MessageWrapper replyWrapper = new MessageWrapper();
                        // game is initialized in the setup
                        replyWrapper.setType(MessageContent.GET_MAP_REPLY);
                        reply.setContentObject(replyWrapper);
                        reply.setPerformative(ACLMessage.INFORM);
                        agent.log("Response to: " + MessageContent.GET_MAP);
                        
                        break;
                }
                
            }
        } catch (UnreadableException |IOException e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.getMessage());
        }

        return reply;
    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}
