package br.edu.ufcg.ic.akka.java;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import br.edu.ufcg.ic.akka.java.Buffer.Input;
import br.edu.ufcg.ic.akka.java.Buffer.Full;

public class Produtor extends UntypedActor{
	LoggingAdapter log;
	private static ActorRef buffer;
	private static Integer producaoTotal;
	
	static public class Produzir {    
        public Produzir() {}
    }
	
	public static Props props() {
        return Props.create(new Creator<Produtor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Produtor create() throws Exception {
                return new Produtor(buffer, producaoTotal);
            }

        });
    }
	
	public Produtor(ActorRef buffer, int producaoTotal) {
		log = Logging.getLogger(getContext().system(), this);
    	Produtor.buffer = buffer;
    	Produtor.producaoTotal = producaoTotal;
    }
	
	public void produzir(){
		for(int i = 1; i <= producaoTotal; i++){
			buffer.tell(new Buffer.Input(i), getSelf());
		}
	}
	
    public void onReceive(Object message) throws Exception {
        if (message instanceof Produzir){
        	produzir();
        }
		if (message instanceof Full) {
			//TO DO
			log.info("O buffer parece estar cheio...");
			
        } else unhandled(message);
    }
}