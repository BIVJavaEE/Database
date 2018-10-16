package database;

import java.sql.SQLException;

import org.h2.tools.Server;

public class DBServer{

	private boolean active; 
	private int port;
	
	public DBServer(int port) {
		this.active = false;
		this.port = port;
	}
	
	public void start() throws SQLException {
        Server.createTcpServer("-tcpPort", Integer.toString(this.port), "-tcpAllowOthers").start();
        this.active = true;
    }

    public void stop() throws SQLException {
        Server.shutdownTcpServer("tcp://localhost:"+this.port, "", true, true);
        this.active = false;
    }
    
    public boolean isActive() {
    	return this.active;
    }
	
}
