package br.edu.ufcg.ic.akka.channel;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;

public class ChannelWriter extends Base{
	
	static public class StartWrite{	}

	private static ActorRef channel;
	
	public ChannelWriter(ActorRef channel) {
		super();
		ChannelWriter.channel = channel;
	}

	public static Props props() {
        return Props.create(new Creator<ChannelWriter>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ChannelWriter create() throws Exception {
                return new ChannelWriter(channel);
            }

        });
    }
	
	public void writeOnChannel(ActorRef channel){
		if(channel != null){
			channel.tell(new Channel.InputEvent(2), getSelf());
		}
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		if(getState() == State.state_input){
			if(message instanceof StartWrite){
				writeOnChannel(channel);
				transition(getState(), message);
			} else {
				syso((String)message);
			}
		}
	}

	public ActorRef getChannel() {
		return channel;
	}

	public void setChannel(ActorRef channel) {
		ChannelWriter.channel = channel;
	}

	@Override
	protected void transition(State old, Object event) {
		if (old == State.state_input && event instanceof StartWrite) {
			//syso("input... state: " + getState());
			setState(State.state_input);
		}
	}
}