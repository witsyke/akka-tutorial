package de.hpi.akka_tutorial.remote.actors;

import java.io.Serializable;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorSelection;
import akka.actor.Props;

public class Slave extends AbstractLoggingActor {

	public static Props props() {
		return Props.create(Slave.class);
	}
	
	public static class Connect implements Serializable {

		private static final long serialVersionUID = -4399047760637406556L;

		private final String masterSystemName;
		private final String masterIP;
		private final String masterPort;
		private final String shepherdName;
		private final String slaveSystemName;
		private final String slaveIP;
		private final String slavePort;
		
		public String getMasterSystemName() {
			return this.masterSystemName;
		}
		
		public String getMasterIP() {
			return this.masterIP;
		}
		
		public String getMasterPort() {
			return this.masterPort;
		}
		
		public String getShepherdName() {
			return this.shepherdName;
		}

		public String getSlaveSystemName() {
			return slaveSystemName;
		}

		public String getSlaveIP() {
			return slaveIP;
		}

		public String getSlavePort() {
			return slavePort;
		}

		public Connect(String masterSystemName, String masterIP, String masterPort, String shepherdName, String slaveSystemName, String slaveIP, String slavePort) {
			this.masterSystemName = masterSystemName;
			this.masterIP = masterIP;
			this.masterPort = masterPort;
			this.shepherdName = shepherdName;
			this.slaveSystemName = slaveSystemName;
			this.slaveIP = slaveIP;
			this.slavePort = slavePort;
		}
	}
	
	public static class Shutdown implements Serializable {
		
		private static final long serialVersionUID = -8962039849767411379L;
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Connect.class, this::handle)
				.match(Shutdown.class, this::handle)
				.matchAny(object -> this.log().info(this.getClass().getName() + " received unknown message: " + object.toString()))
				.build();
	}
	
	private void handle(Shutdown message) {
		
		// Log remote shutdown message
		this.log().info("Remote asked to shutdown this ActorSystem.");
		
		// Shutdown this system
		this.getContext().system().terminate();
	}
	
	private void handle(Connect message) {
		
		// Find the shepherd actor in the remote ActorSystem
		ActorSelection selection = this.getContext().system().actorSelection("akka.tcp://" + message.getMasterSystemName() + "@" + message.getMasterIP() + ":" + message.getMasterPort() + "/user/" + message.getShepherdName());
		
		// Register the local ActorSystem by sending a subscription message
		selection.tell(new Shepherd.Subscription(message.getSlaveSystemName(), message.getSlaveIP(), message.getSlavePort()), this.getSelf());
	}
}