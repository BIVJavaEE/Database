import java.net.UnknownHostException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import database.DBServer;
import mapping.MapperFactoryException;
import mqtt.Subscriber;

public class Main {
	
	public static final String URI = "tcp://ouaz.me";
	public static final String TOPIC = "measures";
	public static final int QOS = 1;
	public static final String DATABASE = "DATABASE";
	
	public static void main(String[] args) throws UnknownHostException, Exception {
		DBServer server = new DBServer(9092);
		server.start();
	    MqttConnectOptions options = new MqttConnectOptions();
	    options.setAutomaticReconnect(true);
	    options.setCleanSession(true);
	    options.setConnectionTimeout(10);
	    
	    Subscriber subscriber = new Subscriber(URI, QOS);
	    try {
			subscriber.connect(options, TOPIC);
		} catch (MqttException e) {
			System.out.println("Connection to message broker failed - reason is: " + e.getMessage());
		} catch (MapperFactoryException e) {
			System.out.println(e.getMessage());
		}
	
	}
}
