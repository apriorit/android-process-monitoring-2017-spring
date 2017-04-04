import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
public class dataBase {
	private static final String url = "jdbc:mysql://localhost:3306/android_registration";
    private static final String user = "root";
    private static final String password = "root";
    
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    
    public dataBase(){
    	 try {
             // opening database connection to MySQL server
             con = DriverManager.getConnection(url, user, password);
  
             // getting Statement object to execute query
             stmt = con.createStatement();

             // executing SELECT query
             rs = stmt.executeQuery("Select * from accounts");
    	 }
    	 catch (SQLException sqlEx) {
             sqlEx.printStackTrace();
           //close connection ,stmt and resultset here
             try { con.close(); } catch(SQLException se) {  }
             try { stmt.close(); } catch(SQLException se) {  }
             try { rs.close(); } catch(SQLException se) {  }
             
         }
    }
    public void printUsersFrom(String account){
         	String query = "select user_name from users where account_login ="+"'"+account+"'";
         	try {
				dataBase.rs = dataBase.stmt.executeQuery(query);
	             while (dataBase.rs.next()) {
	                String name = rs.getString(1);
	              }
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             
    }
    
    public int getUserId(String account, String userName){
    	int returnId = 0;
    	String query = "select id from users "
     			+ "where user_name = "+"'" + userName+ "'"
     			+ "and account_login = "+"'" + account+ "'";
     	try {
			dataBase.rs = dataBase.stmt.executeQuery(query);
             while (dataBase.rs.next()) {
                String id = rs.getString(1);
                returnId = Integer.parseInt(id);
              }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return returnId;
    }
    public HashSet<String> getDevices(int Id){
    	HashSet<String> tokens = new HashSet<String>();
    	String query = "select token_id from user_devices where user_id = " + Id;
     	try {
			dataBase.rs = dataBase.stmt.executeQuery(query);
             while (dataBase.rs.next()) {
                String token = rs.getString(1);
                tokens.add(token);
              }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     	return tokens;
    }
    public HashMap<String, Integer> getDeviceAps(String tokenId,int black_flag){
    	HashMap<String, Integer> packages = new HashMap<String, Integer>();
    	 String query = "select package_name, is_black from device_packages where token_id = " + "'" + tokenId + "'";
         try {
 			dataBase.rs = dataBase.stmt.executeQuery(query);
              while (dataBase.rs.next()) {
                 String packageName = rs.getString(1);
                 int isBlack = rs.getInt(2);
                 //1 = FLAG - BLACK_LIST
                 if(black_flag == 1)
                 {
                	 if(isBlack == 1)
                	 {
                         packages.put(packageName, isBlack);
                	 }
                 }
                 else{
                     packages.put(packageName, isBlack);
                 }
               }
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    	return packages;
    }
    public HashMap<String, HashMap<String,Integer>> getUserAps(int Id){//userAps
    	HashMap<String, HashMap<String,Integer>> packages = new HashMap<String, HashMap<String,Integer>>();
    	HashSet<String> tokens = getDevices(Id);
    	Iterator<String> iterator = tokens.iterator();
        while (iterator.hasNext()) {
        	String tokenId = iterator.next();
            //System.out.printf(" tokenIdVerify: %s %n", tokenId);
            packages.put(tokenId,getDeviceAps(tokenId,0));
        }
        return packages;
    }
    
    public int createAccoutn(String login, String pass, String masterKey)
    {
    		String verifyQuery = "select login  from accounts where accounts.login = '"+ login + "';";
    		
	    	try {
				dataBase.rs = dataBase.stmt.executeQuery(verifyQuery);
				if(dataBase.rs.next()){
					return 1;//account is set
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
			String query = "INSERT INTO accounts VALUES(0,'"+ login + "',"
														+"'"+ pass + "',"
														+"'"+masterKey+ "'"
														+");";
			// executing SELECT query
			try {
				stmt.executeUpdate(query);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	return 0;
    }
    
    public int createUser(String account_login, String user_name){
    	
    	String verifyAccountQuery = "select login from accounts where login = '"+account_login+"';";
		
    	try {
			dataBase.rs = dataBase.stmt.executeQuery(verifyAccountQuery);
			if(!dataBase.rs.next()) return 2;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String verifyQuery = "select account_login,user_name from users where account_login='"+account_login+"' and "
    			+"user_name='"+ user_name +"';";
		
    	try {
			dataBase.rs = dataBase.stmt.executeQuery(verifyQuery);
			if(dataBase.rs.next()){
				return 1; //user is set
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String query = "INSERT INTO users VALUES('" + account_login + "',"
				+"'"+ user_name + "',"
					+0
				+");";
    	try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 0;
    }
    public int connectDevice(int user_id, String token_id){
    	
    	String verifyUserQuery = "select id from users where id = "+user_id+";";
		
    	try {
			dataBase.rs = dataBase.stmt.executeQuery(verifyUserQuery);
			if(!dataBase.rs.next()) return 2;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String verifyConnectQuery = "select user_id,token_id from user_devices where user_id="+user_id+" and "
    			+"token_id='"+ token_id +"';";
		
    	try {
			dataBase.rs = dataBase.stmt.executeQuery(verifyConnectQuery);
			if(dataBase.rs.next()){
				return 1;//device is set
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String verifyTokenQuery = "select token_id from user_devices where token_id='"+ token_id +"';";
		
    	try {
			dataBase.rs = dataBase.stmt.executeQuery(verifyTokenQuery);
			if(dataBase.rs.next()){
				stmt.executeUpdate("delete from user_devices where token_id='"+ token_id +"';");
			}
		} catch (SQLException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	String query = "INSERT INTO user_devices VALUES(" + user_id +","
				+"'"+ token_id + "');";
    	try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 0;
    }
    
    public int writePackeges(HashSet<String> apsDevice, String token_id){
    	HashMap<String, Integer> oldAps = getDeviceAps(token_id,0);//getDeviceAps(token_id);       
       for(Map.Entry<String, Integer> val : oldAps.entrySet())
       {
    	 String packageName = val.getKey();
    	 if(!apsDevice.contains(packageName)){
    		 ///delete in bd
    		 try {
    					stmt.executeUpdate("delete from device_packages where token_id='"+ token_id +"' and "
    																+ "package_name= '"+packageName+"';");
    			} catch (SQLException e) {

    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		 }   
    	 }
    	 Iterator<String> iterator = apsDevice.iterator();
         while (iterator.hasNext()) {
        	String key = iterator.next();
        	 if(!oldAps.containsKey(key))
         	{
         		//write in pd pname and false
         		String query = "INSERT INTO device_packages VALUES('" + token_id + "',"
        				+"'"+ key + "',"
        					+0
        				+");";
            	try {
        			stmt.executeUpdate(query);
        		} catch (SQLException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
         	}
         }
    	return 0;
    }
    public int lockPackeges(HashMap<String, HashMap<String,Integer>> apsUsre){
    	
    	//getDeviceAps(token_id);
    	for(Map.Entry<String, HashMap<String,Integer>> device_apps : apsUsre.entrySet()){
    		String token_id = device_apps.getKey();
    		HashMap<String, Integer> oldAps = getDeviceAps(token_id,0);
    		HashMap<String, Integer> newAps = device_apps.getValue();
    		
    		for(Map.Entry<String, Integer> val : oldAps.entrySet())
    	       {
    			String packageName =val.getKey();
    	    	 if(!newAps.containsKey(val.getKey())){
    	    		 ///delete in bd
    	    		 try {
    	    			 stmt.executeUpdate("delete from device_packages where token_id='"+ token_id +"' and "
									+ "package_name= '"+packageName+"';");
    	    			} catch (SQLException e) {

    	    				// TODO Auto-generated catch block
    	    				e.printStackTrace();
    	    			}
    	    		 }   
    	    	}
    		
    		

    	       for(Map.Entry<String, Integer> val : newAps.entrySet())
    	       {
	    	    	 String packageName = val.getKey();
	    	    	 int bool = val.getValue();
	    	    	 
	    	    	 if(oldAps.containsKey(packageName) && !(bool == oldAps.get(packageName)))
	    	    	 {
	    	    		 try {
							stmt.executeUpdate("delete from device_packages where token_id='"+ token_id +"'and "
										+ "package_name='"+packageName+"';");

		    	    		 String query = "INSERT INTO device_packages VALUES('" + token_id + "',"
		 	        				+"'"+ packageName + "',"
		 	        					+bool
		 	        				+");";
		         			stmt.executeUpdate(query);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    	    	 }  
    	    	 }
    	}
       
    	 
    	return 0;
    }
};
