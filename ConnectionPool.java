/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.westermunion.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

/**
 *
 * @author Victorino
 */
public class ConnectionPool implements LogSource, Configurable{
  private String url, username, password, driver;
  private int maxPoolSize = 0;
  private int minPoolSize = 0;
  private int initialPoolSize = 0;
  private int maxIdleTime = 0;
  private int acquireIncrement = 0;
  private int checkoutTimeout = 0;
  private int maxStatements = 0;
  private int idleConnectionTestPeriod = 0;
    

  ComboPooledDataSource cpds = null;
  
  Configuration cfg;
  Logger logger;
  String realm;

  public ConnectionPool(){
     super();
  }

   public ComboPooledDataSource getConnPoolC3P0( ){
    return cpds;
  }
    
  public void setConfiguration (Configuration cfg)throws ConfigurationException{
    this.cfg = cfg;    
    initEngine();
  }

  private void initEngine () throws ConfigurationException {
    cpds = new ComboPooledDataSource();
    initJDBC();
  }

  private void initJDBC()throws ConfigurationException {
    try {
      driver = cfg.get("jdbc.driver");
      url = cfg.get("jdbc.url");
      username = cfg.get("jdbc.user");
      password = cfg.get("jdbc.password");
      maxPoolSize = cfg.getInt("maxPoolSize", 50);
      minPoolSize = cfg.getInt("minPoolSize", 15);
      maxIdleTime = cfg.getInt("maxIdleTime", 30000);
      initialPoolSize = cfg.getInt("initialPoolSize", 5);
      acquireIncrement = cfg.getInt("acquireIncrement", 2);
      checkoutTimeout = cfg.getInt("checkoutTimeout", 2000);
      maxStatements = cfg.getInt("maxStatements", 200);
      idleConnectionTestPeriod = cfg.getInt("idleConnectionTestPeriod", 120);

      cpds.setDriverClass(driver);
      cpds.setJdbcUrl(url);
      cpds.setUser(username);
      cpds.setPassword(password);
      cpds.setMaxStatements(maxStatements);
      cpds.setMaxPoolSize(maxPoolSize);
      cpds.setMinPoolSize(minPoolSize);
      cpds.setMaxIdleTime(maxIdleTime);
      cpds.setInitialPoolSize(initialPoolSize);
      cpds.setAcquireIncrement(acquireIncrement);
      cpds.setCheckoutTimeout(checkoutTimeout);
      cpds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
     
    } catch (PropertyVetoException ex) {
        LogEvent evt = new LogEvent (this, "error");
        evt.addMessage ("Config Error Property");
        evt.addMessage (ex.getMessage());
        Logger.log (evt);      
    }
 
  }

   public synchronized Connection getConnection() {
     Connection conn = null;
     try{       
          conn = cpds.getConnection();       
     }catch(SQLException ex){
        LogEvent evt = new LogEvent (this, "error");
        evt.addMessage ("Connection limit reached");
        evt.addMessage (ex.getMessage());
        Logger.log (evt);
        return null;
     }

     return conn;
   }

    public synchronized void closeAllConnections() throws SQLException {      
        DataSources.destroy(cpds);
        if(cpds != null)
          cpds.close();
        cpds = null;      
    }

//     public synchronized void free(Connection connection) {
//      try {
//        if(connection != null)
//          connection.close();
//      } catch (SQLException ex) {
//        LogEvent evt = new LogEvent (this, "error");
//        evt.addMessage ("Error Free Connection");
//        evt.addMessage (ex);
//        Logger.log (evt);
//      }
//    }

    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
   


}
