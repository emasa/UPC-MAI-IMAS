/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MessageWrapper;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetInitiator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                        
                        // TODO: copiar este codigo en el mensaje viniendo de los harvesters
                        // cuando el ultimo termina se envia el mensaje al system
                        // USAR agent.setHarvestersFinished(true); en lugar de scouts
                        agent.setScoutsFinished(true);                        
                        if (agent.getScoutsFinished() && agent.getHarvestersFinished()) {
                            agent.addBehaviour(new RequesterBehaviour(agent, agent.createStepFinished()));
                        }
                        
                        reply.setPerformative(ACLMessage.AGREE);
                        break;
                    case MessageContent.NEW_STEP:
                        agent.log("Recieved from System agent new simulation step");
                        agent.setScoutsFinished(false);
                        agent.setHarvestersFinished(false);

                        agent.addBehaviour(new RequesterBehaviour(agent, agent.createNewScoutStepRequest()));
                        agent.addBehaviour(new RequesterBehaviour(agent, agent.createNewHarvesterStepRequest()));
                        
                        // Cycle through garbageBuildings to get cell and start a ContractNet
                        for (int i = 0; i < agent.getGarbageFound().size(); i++) {
                            try{
                                BuildingCell celda = agent.getGarbageFound().get(i);
                                // Fill the CFP message
                                ACLMessage contract = new ACLMessage(ACLMessage.CFP);
                                contract.addReceiver(agent.getHarvesterCoordinatorAgent());             // Receiver is HarvesterCoordinator
                                contract.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);  // ContractNet protocol
                                contract.setReplyByDate(new Date(System.currentTimeMillis() + 10000));  // We want to receive a reply in 10 secs
                                contract.setConversationId("Contract: "+i);                             // ConversationID                    
                                System.out.println("1. "+agent.getLocalName()+": sent contract "+contract.getConversationId());

                                MessageWrapper mmm = new MessageWrapper();
                                mmm.setType(MessageContent.SETTABLE_BUILDING);
                                mmm.setObject(celda);

                                contract.setContentObject(mmm);

                                agent.log("ContractNet Started");                    

                                agent.addBehaviour(new ContractNetInitiator(agent, contract) {
                                    @Override
                                    protected void handlePropose(ACLMessage propose, Vector v) {
                                        // Receive Proposal
                                        System.out.println("3. "+propose.getSender().getName()+": proposed a coalition on "+propose.getConversationId());
                                    }

                                    @Override
                                    protected void handleRefuse(ACLMessage refuse) {
                                        System.out.println("3. "+refuse.getSender().getName()+": refused "+refuse.getConversationId());
                                    }

                                    @Override
                                    protected void handleFailure(ACLMessage failure) {
                                        if (failure.getSender().equals(myAgent.getAMS())) {
                                            // FAILURE notification from the JADE runtime: the receiver
                                            // does not exist
                                            System.out.println("Responder does not exist");
                                        }
                                        else {
                                            System.out.println("Agent "+failure.getSender().getName()+" failed "+failure.getConversationId());
                                        }
                                    }

                                    @Override
                                    protected void handleAllResponses(Vector responses, Vector acceptances) {
                                        // Accept Proposal. CA always accepts proposal
                                        Enumeration e = responses.elements();
                                        while (e.hasMoreElements()) {
                                            ACLMessage proposal = (ACLMessage) e.nextElement();                    
                                            ACLMessage accept = proposal.createReply();
                                            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                            acceptances.addElement(accept);
                                            accept.setContent(proposal.getContent()); 
                                            System.out.println("4. "+myAgent.getLocalName()+": accepted proposal "+proposal.getContent() + " for "+ proposal.getConversationId());
                                        }             
                                    }

                                    @Override
                                    protected void handleInform(ACLMessage inform) {
                                        System.out.println("8. "+inform.getSender().getName()+": successfully performed "+inform.getConversationId());
                                    }
                                });
                            } catch (IOException ex) {
                                Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception e) {
                                agent.errorLog("Incorrect content: " + e.toString());
                            }
                        }
                        reply.setPerformative(ACLMessage.AGREE);
                        break;
                    case MessageContent.HARVESTERS_FINISH:
                        agent.log("Recieved from Harvester Coordinator ");
                        agent.setHarvestersFinished(true);                        
                        if (agent.getScoutsFinished() && agent.getHarvestersFinished()) {
                            agent.addBehaviour(new RequesterBehaviour(agent, agent.createStepFinished()));
                        }
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
