package application;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import database.DBServer;
import mapping.MapperFactoryException;
import mqtt.Subscriber;

public class Main {
	
	public static void initConfiguration(Configuration configuration) throws ConfigurationException {
		System.out.println("Creating configuration file on path: " + configuration.getPath());
		ObjectNode parameters = new ObjectNode(JsonNodeFactory.instance);
		parameters.putObject(("database"))
		.put("autostart", true)
		.put("port", 9092)
		.put("name", "~/DATABASE");
		parameters.putObject("subscriber")
		.put("uri", "localhost")
		.put("qos", 1)
		.put("autoReconnect", true)
		.put("cleanSession", false)
		.put("topic", "")
		.put("uuid", "")
		.put("connexionTimeout", "60");
		configuration.save(parameters);
		System.out.println("Configuration created, please complete it.");
		System.exit(0);
	}
	
	public static void main(String[] args) throws Exception {

        Configuration configuration = new Configuration("config/config.json");
		
        JsonNode config = configuration.read();
        
        if(config == null) {
        	System.out.println("Can't find the configuration file at: " + configuration.getPath());
        	initConfiguration(configuration);
        }
        
        
        JsonNode database = config.get("database");
        JsonNode subscriberConfiguration = config.get("subscriber");
        
		if(database.get("autostart").asBoolean()) {
			DBServer server = new DBServer(database.get("port").asInt());
			server.start();
		}
		
		MqttConnectOptions options = new MqttConnectOptions();
	    options.setAutomaticReconnect(subscriberConfiguration.get("autoReconnect").asBoolean());
	    options.setCleanSession(subscriberConfiguration.get("cleanSession").asBoolean());
	    options.setConnectionTimeout(subscriberConfiguration.get("connexionTimeout").asInt());
	    
	    String uri = subscriberConfiguration.get("uri").asText();
	    String topic = subscriberConfiguration.get("topic").asText();
	    String uuid = subscriberConfiguration.get("uuid").asText();
	    if(uuid.equals("")) {
	    	uuid = UUID.randomUUID().toString();
	    	((ObjectNode) subscriberConfiguration).put("uuid", uuid);
	    	configuration.save();
	    }
	    int qos = subscriberConfiguration.get("qos").asInt();
	    
	    Subscriber subscriber = new Subscriber(database);
	    try {
			subscriber.connect(options, uri, topic, uuid, qos);
		} catch (MqttException e) {
			System.out.println("Connection to message broker failed - reason is: " + e.getMessage());
		} catch (MapperFactoryException e) {
			System.out.println(e.getMessage());
		}
	
	}
}
