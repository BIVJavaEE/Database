package application;

import database.DBServer;
public class Main {
	
	public static void main(String[] args){
		try {
			if(args.length > 0 ) {
				DBServer server = new DBServer(Integer.parseInt(args[0]));
				server.start();
			}else {
				System.out.println("Please add the database port in argument and restart");
			}
		}catch(Exception e) {
			System.out.println("Can't start database: " + e.getMessage());
		}
	}
}
