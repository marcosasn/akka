package br.edu.ufcg.ic.akka.java.fsm;

import java.util.Timer;
import java.util.TimerTask;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import br.edu.ufcg.ic.akka.java.fsm.Buffer.BufferApi.Empty;
import br.edu.ufcg.ic.akka.java.fsm.Buffer.BufferApi.Input;
import br.edu.ufcg.ic.akka.java.fsm.Consumidor.ConsumidorApi.Consumir;
import br.edu.ufcg.ic.akka.java.fsm.Produtor.ProdutorApi.Pausar;
import br.edu.ufcg.ic.akka.java.fsm.Consumidor.ConsumidorApi.TempoEspera;
import br.edu.ufcg.ic.akka.java.fsm.Produtor.ProdutorApi.UseBuffer;
import br.edu.ufcg.ic.akka.java.fsm.Buffer.BufferApi.Output;

public class Consumidor extends BaseConsumidor {
	
	public interface ConsumidorApi {
		
		public static class Consumir {    
	        public Consumir() {}
	    }
		
		public static class TempoEspera {
	        private final int tempo;
	        
	        public TempoEspera(int tempo) {
	            this.tempo = tempo;
	        }

	        public int getTempo() {
	    		return tempo;
	        }
	    }		
	}

	private LoggingAdapter log;
	private static ActorRef buffer;
	private boolean pausado;
	private long espera;
	Timer temporizador = new Timer();
	TimerTask task = new TimerTask(){

		@Override
		public void run() {
			if(!pausado && buffer != null){
				buffer.tell(new Output(), getSelf());
				transition(State.OUTPUT, new Output());
			}
		}		
	};
	
	public static Props props() {
        return Props.create(new Creator<Consumidor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Consumidor create() throws Exception {
                return new Consumidor();
            }

        });
    }
	
	public Consumidor() {
    	log = Logging.getLogger(getContext().system(), this);
    	pausado = false;
    	espera = 0;
    	init();
	}
	
	public void onReceive(Object message) throws Exception {
		if (getState() == State.OUTPUT) {
			if (message instanceof UseBuffer){
	        	buffer = ((UseBuffer)message).buffer;
	        } 
			else if (message instanceof Consumir){
	        	transition(State.OUTPUT, new Output());
	        	if(!pausado && buffer != null){
	        		startConsummation();
	        	}
	        }
	        else if (message instanceof Empty) {
	        	System.out.println("Buffer vazio.");
	        } 
	        else if (message instanceof Pausar) {
	        	if(pausado){
					pausado = false;
					System.out.println("O consumidor foi resumido...");
				}else{
					pausado = true;
					System.out.println("O consumidor foi pausado...");
				}
	        }
			else if(message instanceof Input){
	        	System.out.println("Consumidor recebeu int. input recebido: " + ((Input)message).getNumero());
			} 
			else if (message instanceof TempoEspera) {
				espera = ((TempoEspera)message).getTempo();
				System.out.println("O consumidor recebeu tempo de espera...");
			} else 
				whenUnhandled(message);
		}
    }
	
	private void startConsummation() {	
		try {
			temporizador.scheduleAtFixedRate(task, 10, espera);
		} catch (IllegalStateException e) {
		}
		
		/*for(int i = 0; i < 5; i++){
			buffer.tell(new Output(), getSelf());
		}*/
	}
	
	private void whenUnhandled(Object o) {
		log.warning("received unknown message {} in state {}", o, getState());
	}

	@Override
	protected void transition(State old, Output event) {
		if (old == State.OUTPUT && event instanceof Output) {
			setState(State.OUTPUT);
		}			
	}
}