package org.orienteer.crm.model;


import lombok.Value;

/**
 * Status of a particular {@link IInteraction} 
 */
public enum InteractionStatus {
	CREATED(true, false, false),
	PLANNED(true, false, false),
	SENT(true, true, false),
	ERROR(true, false, false),
	DELIVERED(true, true, true),
	RECEIVED(true, true, true);
	
	private final boolean saved;
	private final boolean distributed;
	private final boolean seen;
	
	private InteractionStatus(boolean saved, boolean distributed, boolean seen) {
		this.saved = saved;
		this.distributed = distributed;
		this.seen = seen;
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	public boolean isDistributed() {
		return distributed;
	}
	
	public boolean isSeen() {
		return seen;
	}
	
	public static InteractionStatus fromTwilioStatus(String status) {
		switch(status) {
			case "sent": return SENT;
			case "delivered": return DELIVERED;
			case "undelivered":
			case "failed":
				return ERROR;
			default:
				return null;
		}
	}
}
