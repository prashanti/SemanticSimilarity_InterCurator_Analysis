package reasoning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class DbConnection {
	
	public  Connection connection()
	 throws ClassNotFoundException, SQLException, IOException {
		Connection con=null;
			File file = new File("db.properties");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			
			 String drivers = properties.getProperty("mySql.driver");
		        String connectionURL = properties.getProperty("mysql.url");
		        String username = properties.getProperty("mySql.user");
		        String password = properties.getProperty("mySql.password");

		        
		        Class.forName(drivers);
		        con=(Connection) DriverManager.getConnection(connectionURL,username,password);

		            System.out.println("Connection Successful");
		            return con; 
		            
		            
		            
		
	}
}

