package br.edu.ufcg.ic.akka.csp.process;

import java.util.ArrayList;
import java.util.List;

import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.Procedure;
import br.edu.ufcg.ic.akka.csp.event.Event;
import br.edu.ufcg.ic.akka.csp.event.TypedEvent;
import br.edu.ufcg.ic.akka.csp.event.UntypedEvent;
import br.edu.ufcg.ic.akka.csp.process.ProcessCSP.ProcessCSPApi.AddInitial;
import br.edu.ufcg.ic.akka.csp.process.ProcessCSP.ProcessCSPApi.Execute;
import br.edu.ufcg.ic.akka.csp.process.ProcessCSP.ProcessCSPApi.GetInitials;
import br.edu.ufcg.ic.akka.csp.process.ProcessCSP.ProcessCSPApi.Initials;
import br.edu.ufcg.ic.akka.csp.process.ProcessCSP.ProcessCSPApi.SetBehavior;
import br.edu.ufcg.ic.akka.csp.process.ProcessCSPBase.State;;

public class ProcessCSP extends ProcessCSPBase {
	
	Procedure<Object> nextBehavior;
	private List<Event> inits;

	public interface ProcessCSPApi {
		public static class Execute {
			public Execute() {
			}
		}

		public static class AddInitial {
			public Event event;

			public AddInitial(Event event) {
				this.event = event;
			}
		}

		public static class Initials {
			public List<Event> events;

			public Initials(List<Event> list) {
				this.events = list;
			}
		}

		public static class GetInterState {
			public GetInterState() {
			}
		}

		public static class InterState {
			public State state;

			public InterState(State state) {
				this.state = state;
			}

			public State getState() {
				return state;
			}
		}
		
		public static class SetBehavior {
			public Procedure<Object> prc;

			public SetBehavior(Procedure<Object> prc) {
				this.prc = prc;
			}

			public Procedure<Object> getBehavior() {
				return prc;
			}
		}

		public static class GetInitials {
			public GetInitials() {
			}
		}
	}

	public ProcessCSP() {
		super();
		super.initialize();
		inits = new ArrayList<Event>();
	}

	public static Props props() {
		return Props.create(new Creator<ProcessCSP>() {
			private static final long serialVersionUID = 1L;

			@Override
			public ProcessCSP create() throws Exception {
				return new ProcessCSP();
			}

		});
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if (getState() == State.started) {
			if (message instanceof AddInitial) {
				inits.add(((AddInitial)message).event);

			} else if (message instanceof GetInitials) {
				getSender().tell(new Initials(inits), getSelf());
			} else if (message instanceof Execute) {
				execute();
				
			} else if (message instanceof SetBehavior){
				nextBehavior = ((SetBehavior)message).getBehavior();
				
			} else if (message instanceof Event) {
				transition(getState(), (Event)message);
			}
		}
	}

	@Override
	protected void transition(State old, Event event) {
		if (old == State.started) {
			if (event instanceof TypedEvent) {
				if(nextBehavior != null && !nextBehavior.equals(super.deadlock)){
					super.setState(State.executing);
					syso(getSelf().path().name() + " got " + ((TypedEvent)event).getMessage() + " state " + getState());
					getContext().become(nextBehavior);
				} else {
					super.setState(State.deadlock);
					syso(getSelf().path().name() + " got " + ((TypedEvent)event).getMessage() + " state deadlock");
					getContext().become(super.deadlock);
				}

			} else if (event instanceof UntypedEvent) {

			}
		}
	}
	
	@Override
	protected List<Event> initials() {
		return inits;
	}
	
	private boolean isCurrenteEvent(String message) {
		if(!inits.isEmpty()){
			return inits.get(0).equals(message);
		}
		return false;
	}
	
	private void execute() {
		if(!inits.isEmpty()){
			super.peform(inits.get(0));
		}
	}
}
