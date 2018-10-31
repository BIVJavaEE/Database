package mqtt;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.JsonNode;

import entity.IEntity;
import entity.Measure;
import entity.Sensor;
import mapping.Mapper;
import mapping.MapperFactory;
import mapping.MapperFactoryException;

public class Handler implements Runnable{
	
	private final BlockingQueue<String> measures = new LinkedBlockingQueue<>();
	private final MapperFactory<Measure> mapperFactory = new MapperFactory<>();
	
	private JsonNode configuration;
	
	private Mapper<Measure> mapper;
	
	public Handler(JsonNode configuration) {
		this.configuration = configuration;
	}
	
	public void initialize() throws MapperFactoryException {
		this.mapper = mapperFactory.createMapper(configuration.get("name").asText());
	}
	
	@Override
	public void run() {
		while(true) {
			String data;
			Measure measure;
			try {
				data = measures.take();
				measure = parse(data);
				this.mapper.save(measure);
		    	System.out.println("Data saved: " + data);
			} catch (InterruptedException e) {
				System.out.println("Error while handling measure: " + e.getMessage());
			}
		}
	}
	
	public void handleMeasure(String measure) {
		try {
			measures.put(measure);
		} catch (InterruptedException e) {
			System.out.println("Impossible to add  " + measure + " to the handler queue for reason: " + e.getMessage());
		}
	}
	
	private Measure parse(String measure){
		String[] data = measure.split(";");		
		Measure res = new Measure();
		Sensor sensor = new Sensor();
		sensor.setId(Integer.parseInt(data[0]));
		res.setSensor(sensor);
		res.setValue(Double.parseDouble(data[1]));
		res.setTimestamp(new Timestamp(new Date().getTime()));
		return res;
	}
	
}
