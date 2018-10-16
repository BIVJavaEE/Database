package mqtt;


import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Parser<T> {
	
	private static final ObjectMapper JSON = new ObjectMapper();
	
	private final Class<T> type;

    public Parser(Class<T> type) {
         this.type = type;
    }
	
	public T parse(String json) throws IOException {
		return JSON.readValue(json, type);
	}
	
}
