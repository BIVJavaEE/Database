package mqtt;

import java.io.IOException;

import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

import org.eclipse.paho.client.mqttv3.*;

import com.fasterxml.jackson.databind.JsonNode;

import entity.Measure;
import mapping.Mapper;
import mapping.MapperFactory;
import mapping.MapperFactoryException;

public class Subscriber implements MqttCallback {
	
	private JsonNode configuration;
	
	// JSON parser to unserialize objects
	private Parser<Measure> parser;
	
	// JPA mapper
	private Mapper<Measure> mapper;
	
	private IMqttClient client;
	

	public Subscriber(JsonNode configuration) {
		this.configuration = configuration;
		this.parser = null;
		this.mapper = null;
	}
	
	protected void initialize() throws MapperFactoryException {
		MapperFactory<Measure> mapperFactory = new MapperFactory<>();
		this.mapper = mapperFactory.createMapper(configuration.get("name").asText());
		this.parser = new Parser<>(Measure.class);
	}
	
	public void connect(MqttConnectOptions options, String uri, String topic, String uuid, int qos) throws MqttException, MapperFactoryException {
		this.client = new MqttClient(uri, uuid);
		this.client.setCallback(this);
		this.client.connect(options);	    
        this.client.subscribe(topic, qos);
	}
	
    public void connectionLost(Throwable cause) {
    	cause.printStackTrace();
        System.out.println("Connection lost because of an unhandled error: " + cause);
    }

    public void messageArrived(String topic, MqttMessage message) throws MqttException {
	    try {
	    	// TODO : Parse message and record data in DB
	    	System.out.println("Data saved: " + message.toString());
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