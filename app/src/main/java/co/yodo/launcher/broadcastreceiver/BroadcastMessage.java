package co.yodo.launcher.broadcastreceiver;

/**
 * All the messages that the broadcast receivers exchange.
 * @author Luis Talavera
 */
public class BroadcastMessage {
	
	/**
	 * It is the ID of the application (the package) used in all the broadcast
	 * messages, to prevent conflict problems from others broadcasts.
	 */
	private static final String BROADCAST_APPID
		= BroadcastMessage.class.getClass().getPackage().getName() + ".";
	
	/**
	 * It is used to send the location of the device.
	 * 
	 * EXTRA - The Location object.
	 */
	public static final String ACTION_NEW_LOCATION
		= BROADCAST_APPID + "ActionNewLocation";
	public static final String EXTRA_NEW_LOCATION
		= BROADCAST_APPID + "ExtraNewLocation";
}
