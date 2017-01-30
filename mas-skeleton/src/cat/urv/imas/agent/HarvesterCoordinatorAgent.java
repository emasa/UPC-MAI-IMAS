/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.MessageWrapper;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Daniel
 */
public class HarvesterCoordinatorAgent extends ImasAgent{
     /**
     * Builds the coordinator agent.
     */
    public HarvesterCoordinatorAgent() {
        super(AgentType.HARVESTER_COORDINATOR);
    }

    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription hca = new ServiceDescription();
        hca.setType(AgentType.HARVESTER_COORDINATOR.toString());
        hca.setName(getLocalName());
        hca.setOwnership(OWNER);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(hca);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }        
        log("Creatad new Harvester Coordinator: " + getLocalName());

        /* ********************************************************************/    
        // contract net system        
        System.out.println("Agent "+getLocalName()+" waiting for CFP...");
  	MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP) );
  		
	addBehaviour(new ContractNetResponder(this, template) {
            @Override
            protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                SettableBuildingCell proposal = null;
                try {
                    // Call evaluateAction to convert cfp to SettableBuildingCell
                    proposal = evaluateAction(cfp);
                    System.out.println("2. "+getLocalName()+": CFP Received from "+cfp.getSender().getName()+". Action is to collect: "+proposal.getMapMessage());
                } catch (UnreadableException ex) {
                    Logger.getLogger(HarvesterCoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("2. "+getLocalName()+": Refuse");
                    throw new RefuseException("evaluation-failed");                    
                }
                // Provide a proposal: HC always accepts proposal           
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                try {
                    propose.setContentObject(proposal);
                } catch (IOException ex) {
                    Logger.getLogger(CoordinatorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }                
                propose.setContent(String.valueOf(proposal));                 
                return propose;              
            }
			
            @Override
            protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                System.out.println("5. "+getLocalName()+": Proposal: "+accept.getContent()+" accepted");
		if (performAction()) {
                    System.out.println("7. "+getLocalName()+": Action successfully performed");
                    ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent(accept.getContent());
                    return inform;
		}
		else {
                    System.out.println("7. "+getLocalName()+": Action execution failed");
                    throw new FailureException("unexpected-error");
		}	
            }
			
            protected void handleRejectProposal(ACLMessage reject) {
                System.out.println("5. "+getLocalName()+": Proposal:"+reject.getContent()+" rejected");
            }
        } );
    }
  
    private SettableBuildingCell evaluateAction(ACLMessage contract) throws UnreadableException {
  	// Convert ACLMessage to SettableBuildingCell
        SettableBuildingCell proposal = (SettableBuildingCell) contract.getContentObject();

  	return proposal;
    }
  
    private boolean performAction() { 
        System.out.println("6. "+getLocalName()+" formed a coalition");
        
  	// Call coalition to collect garbage
//        MessageWrapper messageCoalition = new MessageWrapper.setObject();
        // coalition(cellBuilding)
  	return true;
    }

}