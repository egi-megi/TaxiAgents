package pl.edu.pw.elka.taxiAgents;
import jade.core.Agent;
import jade.core.behaviours.*;

class Looper extends SimpleBehaviour
{
    static String offset = "";
    static long   t0     = System.currentTimeMillis();

    String tab = "" ;
    int    n   = 1;
    long   dt;

    public Looper( Agent a, long dt) {
        super(a);
        this.dt = dt;
        offset += "    " ;
        tab = new String(offset) ;
    }

    public void action()
    {
        System.out.println( tab +
                (System.currentTimeMillis()-t0)/10*10 + ": " +
                myAgent.getLocalName() );
        block( dt );
        n++;
    }

    public  boolean done() {  return n>6;  }

}
