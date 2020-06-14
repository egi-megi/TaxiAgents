package pl.edu.pw.elka.taxiAgents;

import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;

public interface ITestClient {
    ACLMessage runMessage(String dest, Serializable o) throws IOException, InterruptedException;

}
