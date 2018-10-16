package mqtt;

import java.io.IOException;
import java.util.UUID;

import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

import org.eclipse.paho.client.mqttv3.*;

import entity.Measure;
import mapping.Mapper;
import mapping.MapperFactory;
import mapping.MapperFactoryException;

public class Subscriber implements MqttCallback {
	
	// MQTT client id
	private String id;
	
	// MQTT server URI
	private String uri;
	
	// Quality of service
	private int qos;
	
	// JSON parser to unserialize objects
	private Parser<Measure> parser;
	
	// JPA mapper
	private Mapper<Measure> mapper;
	
	private IMqttClient client;
	
	public Subscriber(String uri, int qos) {
		this.id = UUID.randomUUID().toString();
		this.uri = uri;
		this.qos = qos;
		this.parser = null;
		this.mapper = null;
	}
	
	protected void initialize() throws MapperFactoryException {
		MapperFactory<Measure> mapperFactory = new MapperFactory<>();
		this.mapper = mapperFactory.createMapper("~/DATABASE");
		this.parser = new Parser<>(Measure.class);
	}
	
	public void connect(MqttConnectOptions options, String topic) throws MqttException, MapperFactoryException {
		initialize();
		this.client = new MqttClient(this.uri, this.id);
		this.client.setCallback(this);
		this.client.connect(options);	    
        this.client.subscribe(topic, this.qos);
	}
	
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because of an unhandled error: " + cause);
    }

    public void messageArrived(String topic, MqttMessage message) throws MqttException {
	    try {
	    	Measure measure = this.parser.parse(message.toString());
	    	this.mapper.save(measure);
	    	System.out.println("Data saved: " + message.toString());
	    }catch (IOException e) {
			System.out.println("Parsing data failed : " + e.getMessage());
		}catch (IllegalArgumentException | TransactionRequiredException e) {
			System.out.println("Ignoring last sensor data: " + message.toString());
		}catch (PersistenceException e) {
			System.out.println("Ignoring data that not respect model and database constraints. Reasons: " + e.getMessage());
		}catch (IllegalStateException e) { // If database state is invalid, stop the listening of sensors
			e.printStackTrace();
		}
    }

	public void deliveryComplete(IMqttDeliveryToken token) {
		// Do nothing, this class is only a receiver
	}
	
}