/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.westermunion.listener;




import com.westermunion.db.ConnectionPool;
import com.westermunion.main.InitConfig;
import com.westermunion.util.transaccionalWU;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Hashtable;
import javax.swing.Timer;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.q2.Q2;
import org.jpos.util.Log;
import org.jpos.util.ThreadPool;

/**
 *
 * @author Victorino
 */
public class ServerProcessListener implements ISORequestListener{

  private String nombreServer = null;
  private Hashtable informacion = null;

  private Log log = null;

  private ThreadPool threadPool = null;

  static public int numeroTrx;
  static public int contaTrx;
  static public int tiempoEspera;
  static public boolean activarControl;
  static public boolean flagBloqueo;
  static public boolean flagBaseBloqueo;

  static public Timer timer = null;

  static public  int flagAlarma= 0;
  static final public  int NONE = 0;
  static final public  int ACTIVADA = 1;
  static final public  int DESACTIVADA = 2;

  private ConnectionPool connPool2005 = null;

  //private TransaccionGeneral trx = null;

  public ServerProcessListener(String nombreServer,Hashtable informacion){
    this.nombreServer = nombreServer;
    this.informacion = informacion;
    init();

    ServerProcessListener.tiempoEspera = (Integer)informacion.get("tiempoEspera");


     ServerProcessListener.timer = new Timer (ServerProcessListener.tiempoEspera*60000, new ActionListener (){
      public void actionPerformed(ActionEvent e){
        log.warn("Host Desbloqueado ", new Date());
        ServerProcessListener.timer.stop();
      }
    });

  }

  private void init( ){
    int minPool = 0;
    int maxPool = 0;

    minPool = (Integer)informacion.get("initSessions");
    maxPool = (Integer)informacion.get("maxSessions" );
    log = Log.getLog(Q2.LOGGER_NAME,Q2.REALM);
    // Pool de Hilos
    threadPool = new ThreadPool(minPool,maxPool,"ThreadEasyCash");
    InitConfig.loadConfig();

    connPool2005 = InitConfig.getConnectionPool2005();

    informacion.put("connPool2005", connPool2005);

  }

  public boolean process(ISOSource server, ISOMsg msg) {

    String mti = null;
    ISOMsg request = msg;
    ISOMsg response = msg;
    TransaccionGeneral trx = null;

    try{
      mti = request.getMTI();
    }catch(Exception ex){
      log.error("Error mti : " + request, ex);
      mti = null;
    }

    if( mti != null ){
      if(mti.compareTo("0800") == 0){
        try{
          response.setResponseMTI();
          response.set(12,ISODate.getTime(new Date()));
          response.set(13,ISODate.getDate(new Date()));
          response.set(39,"00");
          server.send(response);
        }catch(Exception ex){
          log.error(request, ex);
        }
      }else if(mti.compareTo("0200") == 0 || mti.compareTo("0400") == 0){
        String codigoProceso = null;
        trx = new TransaccionGeneral();

        if( request.hasField(3)){
          codigoProceso= request.getString(3);
        }else{
          codigoProceso = "      ";
        }

        if(codigoProceso.compareTo("370000") == 0  && mti.compareTo("0400")==0 ){
        	try{
        		response.setResponseMTI();
                response.set(39,"A5");
                if(server.isConnected() ){
                	server.send(response);
                }else{
                      log.error(response, "Socket desconectado...");
                }
              }catch(Exception ex){
                log.error(request, ex);
              }
        }else{
            if(codigoProceso.compareTo("280000")==0 ||  codigoProceso.compareTo("300000")==0 ){
                if( request.getString(2).compareTo("827727")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_AGUAPEN_EP" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_AGUAPEN_EP","035"));
                }else if( request.getString(2).compareTo("827728")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_AGUAS_MACHALA" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_AGUAS_MACHALA","126"));
                }else if( request.getString(2).compareTo("827729")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_ARTEFACTA" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_ARTEFACTA","040"));
                }else if( request.getString(2).compareTo("827730")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_AZZORTI" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_AZZORTI","065"));
                }else if( request.getString(2).compareTo("827731")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_BELCORP" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_BELCORP","014"));
                }else if( request.getString(2).compareTo("827732")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_CENTROSUR" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_CENTROSUR","000"));
                }else if( request.getString(2).compareTo("827733")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_CLARO_DTH" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_CLARO_DTH","084"));
                }else if( request.getString(2).compareTo("827734")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_CLARO_SERVICIOS_FIJOS" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_CLARO_SERVICIOS_FIJOS","018"));
                }else if( request.getString(2).compareTo("827735")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_DIRECTV" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_DIRECTV","006"));
                }else if( request.getString(2).compareTo("827736")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_EMOV" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_EMOV","134"));
                }else if( request.getString(2).compareTo("827737")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_ETAFASHION_PLANETA" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_ETAFASHION_PLANETA","032"));
                }else if( request.getString(2).compareTo("827738")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_HERBALIFE" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_HERBALIFE","118"));
                }else if( request.getString(2).compareTo("827739")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_IECE" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_IECE","112"));
                }else if( request.getString(2).compareTo("827740")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_IESS" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_IESS","092"));
                }else if( request.getString(2).compareTo("827741")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_JARDINES_ESPERANZA" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_JARDINES_ESPERANZA","049"));
                }else if( request.getString(2).compareTo("827742")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_LEONISA" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_LEONISA","087"));
                }else if( request.getString(2).compareTo("827743")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_LEUDINE" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_LEUDINE","079"));
                }else if( request.getString(2).compareTo("827744")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_NETLIFE" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_NETLIFE","060"));
                }else if( request.getString(2).compareTo("827745")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_OMNILIFE" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_OMNILIFE","088"));
                }else if( request.getString(2).compareTo("827746")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_ORIFLAME" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_ORIFLAME","016"));
                }else if( request.getString(2).compareTo("827747")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_RM" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_RM","130"));
                }else if( request.getString(2).compareTo("827748")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_TVCABLE" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_TVCABLE","063"));
                }else if( request.getString(2).compareTo("827749")==0 ){
                    log.info("Servicio : " +  request.toString() + " WESTERM_UNIVISA" );
                    threadPool.execute(new transaccionalWU(informacion,server,request, trx,"WESTERM_UNIVISA","021"));
                }else{
                	try{
                        response.setResponseMTI();
                        // Transaccion no soportada
                        response.set(39,"21");
                        if( server.isConnected() ){
                          server.send(response);
                        }else{
                          log.error(response, "Socket desconectado...");
                        }
                	}catch(Exception ex){
                		log.error(request, ex);
                	}
            	}
            }else{
               try{
        	   		response.setResponseMTI();
        	   		// Transaccion no soportada
        	   		response.set(39,"A1");
        	   		if( server.isConnected() ){
        	   			server.send(response);
        	   		}else{
        	   			log.error(response, "Socket desconectado...");
        	   		}
               }catch(Exception ex){
            	   log.error(request, ex);
               }
            }
    	}
      }else{
    	  	try{
    	  		response.setResponseMTI();
    	  		// Transaccion no soportada
    	  		response.set(39,"A1");
    	  		if( server.isConnected() ){
    	  			server.send(response);
    	  		}else{
    	  			log.error(response, "Socket desconectado...");
    	  		}
    	  	}catch(Exception ex){
    	  		log.error(request, ex);
    	  	}
      	}
    }
    return true;
  }

  public String getNombreServer() {
      return nombreServer;
  }

  public void setNombreServer(String nombreServer) {
      this.nombreServer = nombreServer;
  }
  public ConnectionPool getConnPool2005() {
      return connPool2005;
  }
  public void setConnPool2005(ConnectionPool connPool2005) {
      this.connPool2005 = connPool2005;
  }
}
