package com.westermunion.util;

import java.io.Writer;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.iso.ISOCurrency;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.util.Log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.westermunion.DTO.DatosResponseWesterUnionDTO;
import com.westermunion.DTO.InformacionRegistroBaseDTO;
import com.westermunion.XMLFactura.Detalle;
import com.westermunion.XMLFactura.Detalles;
import com.westermunion.XMLFactura.Factura;
import com.westermunion.XMLFactura.XMLFactura;
import com.westermunion.XMLOtros.ReciboEnterprise;
import com.westermunion.XMLOtrosRecibos.XMLOtrosRecibo;
import com.westermunion.XMLRecibo.Cabecera;
import com.westermunion.XMLRecibo.Recibo;
import com.westermunion.db.ConnectionPool;
import com.westermunion.listener.ServerProcessListener;
import com.westermunion.listener.TransaccionGeneral;
import com.westermunion.ws.desarrollo.ClsConsultRequest;
import com.westermunion.ws.desarrollo.ClsConsultResponse;
import com.westermunion.ws.desarrollo.ClsInvoiceInformation;
import com.westermunion.ws.desarrollo.ClsPayRequest;
import com.westermunion.ws.desarrollo.ClsPayResponse;
import com.westermunion.ws.desarrollo.ClsReverseRequest;
import com.westermunion.ws.desarrollo.ClsReverseResponse;
import com.westermunion.ws.desarrollo.Servicios;
import com.westermunion.ws.desarrollo.ServiciosSoap;

public class transaccionalWU implements Runnable{
	
 	@SuppressWarnings("rawtypes")
	private Hashtable 	informacion 		= null;
    private ISOSource 	server 				= null;
    private ISOMsg 		request 			= null;
    private ISOMsg 		response 			= null;
    private Integer 	agencia				= 4963;
    private String 		operador			= "0Z94";
    private String 		terminal			= "A3A0";
    private Integer 	codigoSeguridad		= 41782063;
    private String		codigoCarrier		= "64";
    private String		canalV				= "007";
    private TransaccionGeneral trx 			= null;
    private Log log 						= null;
    @SuppressWarnings("unused")
	private Log log_tout 					= null;
    private ConnectionPool connPool2005		= null;  
    private Connection conn2005 			= null;
    private long tiempoMaximoTrx 			= 80000;
    private final int DB_2005 				= 1;
    String Empresa 							= "";
    String codEmpresaWU						= "";
    String separador						= ";";
    String transDeclinada					= "21";
    String timeOut							= "09";
    String errorJava						= "96";
    String errorBase						= "28";
    String errorTransaccion					= "27";
    String aprobado							= "00";
    String datosAdicionales					= "";
    int numeroMaxDato						= 999;
		
	public transaccionalWU(@SuppressWarnings("rawtypes") Hashtable informacion,ISOSource server,ISOMsg request, TransaccionGeneral trx, String westerm_empresa, String codigoEmpresa){
        this.informacion 		= informacion;
        this.server 			= server;
        this.request 			= request;
        this.response 			= request;
        this.trx 				= trx;
        this.Empresa			= westerm_empresa;
        this.codEmpresaWU		= codigoEmpresa;
        log 					= org.jpos.util.Log.getLog(Q2.LOGGER_NAME,Q2.REALM);
        log_tout 				= org.jpos.util.Log.getLog("Q21_TOUT",Q2.REALM);
        
        ServerProcessListener.numeroTrx 		= (Integer)informacion.get("numeroTrx");
        ServerProcessListener.tiempoEspera 		= (Integer)informacion.get("tiempoEspera");
        ServerProcessListener.activarControl 	= (Boolean)informacion.get("activarControl");

        try{
        	connPool2005 = (ConnectionPool) this.informacion.get("connPool2005");
        }catch(Exception ex){
        	log.error("connPool2005", ex);
        }
	}
	
	public void homologaCodigoCNEL()throws Exception{
		String tempCampo117 = "";
		String datoProvinciaCNEL = "";
		try{
			if(request.hasField(117)){
      	    	tempCampo117 = request.getString(117);
      	    	if(tempCampo117.contains(separador)){
	      	    	tempCampo117 = tempCampo117.substring(tempCampo117.indexOf(separador)+1);
	      	    }
      	    }else{
      	    	tempCampo117 = "";
      	    }
			
			if(tempCampo117 !=""){
	      	    JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, "CON");
	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
	  		  		if(!dato117.getString("referenciaAd").isEmpty()){
	  		  			datoProvinciaCNEL = dato117.getString("referenciaAd");
	  		  		}
	  		  	}
      	    }
			
			if(datoProvinciaCNEL.equals("101")){
				this.codEmpresaWU="009"; 
				this.Empresa = "WESTERN_CNEL_GUAYASRIOS";
			}
			if(datoProvinciaCNEL.equals("102")){
				this.codEmpresaWU="061"; 
				this.Empresa = "WESTERN_CNEL_LOSRIOS";
			}
			if(datoProvinciaCNEL.equals("103")){
				this.codEmpresaWU="111"; 
				this.Empresa = "WESTERN_CNEL_GUAYAQUIL";
			}
			if(datoProvinciaCNEL.equals("501")){
				this.codEmpresaWU="010"; 
				this.Empresa = "WESTERN_CNEL_ELORO";
			}
			if(datoProvinciaCNEL.equals("201")){
				this.codEmpresaWU="025"; 
				this.Empresa = "WESTERN_CNEL_ESMERALDAS";
			}
			if(datoProvinciaCNEL.equals("301")){
				this.codEmpresaWU="011"; 
				this.Empresa = "WESTERN_CNEL_SANTAELENA";
			}
			if(datoProvinciaCNEL.equals("401")){
				this.codEmpresaWU="051"; 
				this.Empresa = "WESTERN_CNEL_MANABI";
			}
			if(datoProvinciaCNEL.equals("601")){
				this.codEmpresaWU="007"; 
				this.Empresa = "WESTERN_CNEL_MILAGRO";
			}
			if(datoProvinciaCNEL.equals("701")){
				this.codEmpresaWU="115"; 
				this.Empresa = "WESTERN_CNEL_SUCUMBIOS";
			}
			if(datoProvinciaCNEL.equals("801")){
				this.codEmpresaWU="129"; 
				this.Empresa = "WESTERN_CNEL_BOLIVAR";
			}
			if(datoProvinciaCNEL.equals("901")){
				this.codEmpresaWU="026"; 
				this.Empresa = "WESTERN_CNEL_STODOMINGO";
			}
			
		}catch(Exception e) {
    		log.error(this, e.getMessage().toString());
    		e.printStackTrace();
        }
		
	}
		
	public boolean validaTiempoConexiones(Date inicioTrx, Date finTrx, long tiempo,ISOMsg msg , int tipoBase){
        long resultado 		= 0;
        String nombreDB 	= null;
        resultado 			= finTrx.getTime() - inicioTrx.getTime();
        nombreDB 			= "SQL2005";
        if( resultado > tiempo){
        	log.warn("Tiempo Excedido de Transaccion " + nombreDB + "  : " + msg, " Tiempo Total : " + resultado + "  Tiempo Maximo : " + tiempo);
        	return false;
        }
        return true;
   }

    public boolean obtenerConexiones( ConnectionPool connPool2005 ) throws SQLException{
    	log.info("Obteniendo Conexion SQL2005 " +  request.toString() + " " + this );
        log.info("connPool2005 IN : " + request.toString());
        try{
        	synchronized( connPool2005 ){
        		conn2005 = connPool2005.getConnection();
        	}
        }catch(Exception ex){      
        	log.error(request.toString(),ex);
        }
        log.info("connPool2005 OUT : " + request.toString());
        if( conn2005 != null){
        	if( validaTiempoConexiones(trx.getInicioTrx(), trx.getFinalTrx() , tiempoMaximoTrx, request , DB_2005) ){
        		return true;
        	}else{
        		cerrarConexionBaseDatos(request);
        		ServerProcessListener.flagBaseBloqueo = true;
        		return false;
        	}
        }else{
        	ServerProcessListener.flagBaseBloqueo = true;
        	return false;
        }
    }
	    
    void cerrarConexionBaseDatos(ISOMsg request)throws SQLException{
    	try {
    		if(conn2005 != null){
    			conn2005.close();
    			conn2005 = null;
    			log.info("cerrarConexionBaseDatos 2005 : " , request.toString());
    		}
    	}catch(SQLException ex) {
           log.error("cerrarConexionBaseDatos conection2005", ex);
        }
    }
	       
    public void responseServer(String codResp) throws Exception{          
    	try{
    		if( response.isRequest()){
    			response.setResponseMTI();                        
    		}
    		response.set(39, codResp);
    		if( server.isConnected() ){
    			try{
    				server.send(response);
    			}catch(Exception ex){
                     log.error(this + "  " + request.toString() , ex);
    			}
    		}
    	}catch(Exception ex){
    		log.error(this, ex);
    	}          
    }
	    
    public static XMLGregorianCalendar getXmlGregorianCalendarFromDate(final Date date) throws DatatypeConfigurationException{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}
	
    private ClsConsultRequest armaDatosConsulta(InformacionRegistroBaseDTO informacionRegistroBaseDTO) throws DatatypeConfigurationException{
	  	
    	ClsConsultRequest  datosConsulta			= null;
        String codigoInstitucion					= this.codEmpresaWU;
        XMLGregorianCalendar FechaTransaccion 		= getXmlGregorianCalendarFromDate(new Date());
        String tempCampo117							= null;
		String tipoProceso  						= "CON";
        
        try{
        	//DECLARACION DE VARIABLES
        	datosConsulta 	= new ClsConsultRequest();
        	datosConsulta.setCarCoeId(codigoInstitucion);
        	datosConsulta.setCarType("");
    		datosConsulta.setCarTypeDescription("");
    		datosConsulta.setCarDate(FechaTransaccion);
    		datosConsulta.setCarAgency(agencia);
    		datosConsulta.setCarOperator(operador);
    		datosConsulta.setCarTerminal(terminal);
    		datosConsulta.setCarSequential(Integer.valueOf(informacionRegistroBaseDTO.getSecuencialBR()));
    		datosConsulta.setCarSequentialSARA(0);
    		datosConsulta.setCarAutorizacion(0);
    		datosConsulta.setCarSecurity(codigoSeguridad);
    		datosConsulta.setCarCarrier(codigoCarrier);
    		datosConsulta.setCarCanal(canalV);
    		
    		if(request.hasField(117)){
      	    	tempCampo117 = request.getString(117);
      	    	if(tempCampo117.contains(separador)){
	      	    	tempCampo117 = tempCampo117.substring(tempCampo117.indexOf(separador)+1);
	      	    }
      	    }else{
      	    	tempCampo117 = "";
      	    }
    		String contrapartida = request.getString(112).substring(4).trim();
    		
    		//SOLO CUENTA
    		//AGUAPEN //AZZORTI //BELCORP //CLARO - DTH //CLARO - SERVICIOS FIJOS //DIRECTV
    		//ETAFASHION PLANETA //HERBALIFE //JARDINES DE ESPERANZA //LEUDINE //NETLIFE
    		//ORIFLAME //REBAJA MODA //TV CABLE //UNIVISA //CENTRO SUR (BP) //IECE // AVON
    		if(codigoInstitucion == "035" || codigoInstitucion == "065" ||		
    		   codigoInstitucion == "014" || codigoInstitucion == "084" ||
    		   codigoInstitucion == "018" || codigoInstitucion == "006" ||
    		   codigoInstitucion == "032" || codigoInstitucion == "118" ||
    		   codigoInstitucion == "049" || codigoInstitucion == "079" ||
    		   codigoInstitucion == "060" || codigoInstitucion == "016" ||
    		   codigoInstitucion == "130" || codigoInstitucion == "063" ||
    		   codigoInstitucion == "021" || codigoInstitucion == "099" ||  
    		   codigoInstitucion == "112" || codigoInstitucion == "003" ||
    		   codigoInstitucion == "CNL" ){
    			
    			datosConsulta.setCarCuenta(contrapartida);
    			
    		}else if(codigoInstitucion == "087"){//DOCUMENTO Y NOMBRE
    			//LEONISA
    			datosConsulta.setCarDocumentId(contrapartida);
        		if(tempCampo117 !=""){
    	      	    JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
    	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
    	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
    	  		  		if(!dato117.getString("nombreContraPartida").isEmpty()){
    	  		  			datosConsulta.setCarNames(dato117.getString("nombreContraPartida"));
    	  		  		}
    	  		  	}
          	    }
    		}else if(codigoInstitucion == "040"){//DOCUMENTO, NOMBRES Y CUENTA
    			//ARTEFACTA
    			datosConsulta.setCarDocumentId(contrapartida);
        		if(tempCampo117 !=""){
    	      		JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
    	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
    	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
    	  		  		if(!dato117.getString("nombreContraPartida").isEmpty()){
    	  		  			datosConsulta.setCarNames(dato117.getString("nombreContraPartida"));
    	  		  		}
	    	  		  	if(!dato117.getString("referencia").isEmpty()){
		  			  		datosConsulta.setCarCuenta(dato117.getString("referencia"));
		  			  	}
    	  		  	}
    	  	    }
    		}else if(codigoInstitucion == "040"){//CUENTA Y NOMBRES
    			datosConsulta.setCarCuenta(contrapartida);
        		if(tempCampo117 !=""){
    	      	    JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
    	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
    	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
    	  		  		if(!dato117.getString("nombreContraPartida").isEmpty()){
    	  		  			datosConsulta.setCarNames(dato117.getString("nombreContraPartida"));
    	  		  		}
    	  		  	}
          	    }
    		}else{
    			
    		}
    	}catch(Exception ex){
            log.error(this, ex);
            datosConsulta = null;
        }
       return datosConsulta;
    }
    
    private ClsPayRequest armaDatosPago(InformacionRegistroBaseDTO informacionRegistroBaseDTO) throws DatatypeConfigurationException{
    	ClsPayRequest datosPago 					= null;
    	String codigoInstitucion					= this.codEmpresaWU;
        XMLGregorianCalendar FechaTransaccion 		= getXmlGregorianCalendarFromDate(new Date());
        ClsInvoiceInformation clsInvoiceInformation = new ClsInvoiceInformation();
    	try{
    		datosPago 			 = new ClsPayRequest();
    		
    		datosPago.setCarCoeId(codigoInstitucion);
    		//datosPago.setCarCuenta(cuentaCliente);
    		datosPago.setCarDate(FechaTransaccion);
    		datosPago.setCarSequential(Integer.valueOf(informacionRegistroBaseDTO.getSecuencialBR()));
    		datosPago.setCarSequentialSARA(Integer.valueOf(informacionRegistroBaseDTO.getSecuencialSARA()));
    		datosPago.setCarAutorizacion(0);
    		datosPago.setCarSecurity(codigoSeguridad);
    		datosPago.setCarType(informacionRegistroBaseDTO.getTipoServicio());
    		datosPago.setCarTypeDescription(informacionRegistroBaseDTO.getDescripcionServicio());
    		datosPago.setCarAgency(agencia);
    		datosPago.setCarOperator(operador);
    		datosPago.setCarTerminal(terminal);
    		datosPago.setCarCarrier(codigoCarrier);
    		datosPago.setCarCanal(canalV);
    		datosPago.setCarOtros("");
    		
    		String tempCampo117	= null;
    		String tipoProceso  = "CON";
      	    if(request.hasField(117)){
      	    	tempCampo117 = request.getString(117);
      	    }else{
      	    	tempCampo117 = "";
      	    }
      	    
      	    String contrapartida = request.getString(112).substring(4).trim();
      	    String tipoDocumento = "C"; //CEDULA
      	  	//SOLO CUENTA
    		//AGUAPEN //AZZORTI //BELCORP //CLARO - DTH //CLARO - SERVICIOS FIJOS //DIRECTV
    		//ETAFASHION PLANETA //HERBALIFE //JARDINES DE ESPERANZA //LEUDINE //NETLIFE
    		//ORIFLAME //REBAJA MODA //TV CABLE //UNIVISA //CENTRO SUR (BP) //IECE
    		if(codigoInstitucion == "035" || codigoInstitucion == "065" ||		
    		   codigoInstitucion == "014" || codigoInstitucion == "084" ||
    		   codigoInstitucion == "018" || codigoInstitucion == "006" ||
    		   codigoInstitucion == "032" || codigoInstitucion == "118" ||
    		   codigoInstitucion == "049" || codigoInstitucion == "079" ||
    		   codigoInstitucion == "060" || codigoInstitucion == "016" ||
    		   codigoInstitucion == "130" || codigoInstitucion == "063" ||
    		   codigoInstitucion == "021" || codigoInstitucion == "099" ||  
    		   codigoInstitucion == "112" || codigoInstitucion == "003" ){
    			
    			datosPago.setCarCuenta(contrapartida);
    			
    		}else if(codigoInstitucion == "087"){//DOCUMENTO Y NOMBRE
    			//LEONISA
    			if(contrapartida.length()>10){
    				tipoDocumento = "R";
    			}
    			datosPago.setCarTypeDocument(tipoDocumento);
    			datosPago.setCarDocumentId(contrapartida);
    			if(tempCampo117 !=""){
    	      	    JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
    	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
    	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
    	  		  		if(!dato117.getString("nombreContraPartida").isEmpty()){
    	  		  			datosPago.setCarNames(dato117.getString("nombreContraPartida"));
    	  		  		}
    	  		  	}
          	    }
    		}else if(codigoInstitucion == "040"){//DOCUMENTO, NOMBRES Y CUENTA
    			//ARTEFACTA
    			if(contrapartida.length()>10){
    				tipoDocumento = "R";
    			}
    			datosPago.setCarTypeDocument(tipoDocumento);
    			datosPago.setCarDocumentId(contrapartida);
        		if(tempCampo117 !=""){
    	      		JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
    	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
    	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
    	  		  		if(!dato117.getString("nombreContraPartida").isEmpty()){
    	  		  			datosPago.setCarNames(dato117.getString("nombreContraPartida"));
    	  		  		}
	    	  		  	if(!dato117.getString("referencia").isEmpty()){
	    	  		  		datosPago.setCarCuenta(dato117.getString("referencia"));
		  			  	}
    	  		  	}
    	  	    }
    		}else if(codigoInstitucion == "040"){//CUENTA Y NOMBRES
    			datosPago.setCarCuenta(contrapartida);
        		if(tempCampo117 !=""){
    	      	    JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
    	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
    	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
    	  		  		if(!dato117.getString("nombreContraPartida").isEmpty()){
    	  		  			datosPago.setCarNames(dato117.getString("nombreContraPartida"));
    	  		  		}
    	  		  	}
          	    }
    		}else{
    			
    		}
      	   
      	    double montoPagar 	= 0;
			montoPagar 		= ISOCurrency.convertFromIsoMsg(request.getString(4), "USD");
			datosPago.setCarValueEfective(BigDecimal.valueOf(montoPagar));
    		
    		clsInvoiceInformation.setCusDocumentId(datosPago.getCarDocumentId());
    		clsInvoiceInformation.setCusTypeDocument(datosPago.getCarTypeDocument());
    		
    	    datosPago.setInvInvoiceInformation(clsInvoiceInformation);
    		
    	}catch (Exception e){
    		log.error(this, e);
    		datosPago = null;
    	}
    	return datosPago;
    }
    
    private ClsReverseRequest armaDatosReverso(InformacionRegistroBaseDTO informacionRegistroBaseDTO) throws DatatypeConfigurationException{
    	ClsReverseRequest datosReverso 				= null;
    	String codigoInstitucion					= this.codEmpresaWU;
        XMLGregorianCalendar FechaTransaccion 		= getXmlGregorianCalendarFromDate(new Date());
        try{
    		datosReverso 			 = new ClsReverseRequest();
    		String cuentaCliente = request.getString(112).substring(4).trim();
    		
    		datosReverso.setCarCoeId(codigoInstitucion);
    		datosReverso.setCarCuenta(cuentaCliente);
    		datosReverso.setCarType(informacionRegistroBaseDTO.getTipoServicio());
    		datosReverso.setCarTypeDescription(informacionRegistroBaseDTO.getDescripcionServicio());
    		datosReverso.setCarDate(FechaTransaccion);
    		datosReverso.setCarAgency(agencia);
    		datosReverso.setCarOperator(operador);
    		datosReverso.setCarTerminal(terminal);
    		datosReverso.setCarSequential(Integer.valueOf(informacionRegistroBaseDTO.getSecuencialBR()));
    		datosReverso.setCarSequentialSARA(Integer.valueOf(informacionRegistroBaseDTO.getSecuencialSARA()));
    		datosReverso.setCarAutorizacion(Integer.valueOf(informacionRegistroBaseDTO.getAutorizacionSARA()));
    		datosReverso.setCarSecurity(codigoSeguridad);
    		datosReverso.setCarCarrier(codigoCarrier);
    		datosReverso.setCarCanal(canalV);
    		//datosReverso.setCarNames(informacionRegistroBaseDTO.getNombreCliente());
    		
    		String tempCampo117	= null;
    		String tipoProceso  = "CON";
      	    if(request.hasField(117)){
      	    	tempCampo117 = request.getString(117);
      	    }else{
      	    	tempCampo117 = "";
      	    }
      	    if(tempCampo117 !=""){
	      	    if(tempCampo117.contains(separador)){
	      	    	tempCampo117 = tempCampo117.substring(tempCampo117.indexOf(separador)+1);
	      	    }
	    		JSONArray datosCampo117 = (JSONArray)SeparaDatosCampo117(tempCampo117, tipoProceso);
	  		  	for (int i = 0; i < datosCampo117.length(); i++) {
	  		  		JSONObject dato117 = datosCampo117.getJSONObject(i);
	  		  		if(!dato117.getString("numeroId_Cl").isEmpty()){
	  			  		//numero de documento
	  			  	datosReverso.setCarDocumentId(dato117.getString("numeroId_Cl")); 
	  			  	}
	  		  	}
      	    }
    	}catch (Exception e){
    		log.error(this, e);
    		datosReverso = null;
    	}
    	return datosReverso;
    }
    
    private DatosResponseWesterUnionDTO manejoDatosRespuestaConsulta(ClsConsultResponse datos) throws Exception{
    	SimpleDateFormat sdf 		= new SimpleDateFormat("MM/dd/yyyy hh:mm");
    	DatosResponseWesterUnionDTO datosResponseWesterUnionDTO = new DatosResponseWesterUnionDTO();
    	try{
    		datosResponseWesterUnionDTO.setIdRegistroActualiza(String.valueOf(datos.getCarSequential()));
			datosResponseWesterUnionDTO.setSecuenciaBR(String.valueOf(datos.getCarSequential()));
			datosResponseWesterUnionDTO.setSecuenciaSara(String.valueOf(datos.getCarSequentialSARA()));
			datosResponseWesterUnionDTO.setAutorizacionSara(String.valueOf(datos.getCarAutorizacion()));
    		if(datos.isSucess()){
    			datosResponseWesterUnionDTO.setFechaSARA(sdf.format(datos.getCarDateSARA().toGregorianCalendar().getTime()));
    			datosResponseWesterUnionDTO.setCuentaCliente(datos.getCarCuenta());
    			datosResponseWesterUnionDTO.setTipoPago(datos.getCarType());
    			datosResponseWesterUnionDTO.setDescripcionPago(datos.getCarTypeDescription());
    			datosResponseWesterUnionDTO.setNombreCliente(datos.getCarNames());
    			datosResponseWesterUnionDTO.setDocumentoCliente(datos.getCarDocumentId());
    			datosResponseWesterUnionDTO.setObservacionPago(datos.getCarObservation());
    			datosResponseWesterUnionDTO.setDireccionObser(datos.getCarAdress());
    			datosResponseWesterUnionDTO.setValorPagar(String.valueOf(datos.getCarValuePaid().doubleValue()));
    			datosResponseWesterUnionDTO.setValorAbonadoCliente(String.valueOf(datos.getCarValueEfective().doubleValue()));
    			datosResponseWesterUnionDTO.setValorTotalPagar(String.valueOf(datos.getCarValueTotal().doubleValue()));
    			datosResponseWesterUnionDTO.setValorminimoPagar(String.valueOf(datos.getCarValueMin().doubleValue()));
    			datosResponseWesterUnionDTO.setValorRecargo(String.valueOf(datos.getCarValueActivaCharge().doubleValue()));
    			datosResponseWesterUnionDTO.setFechaVencimientoPago(sdf.format(datos.getCarDateVen().toGregorianCalendar().getTime()));
    			datosResponseWesterUnionDTO.setNumFactura(datos.getCarFactura());
    			datosResponseWesterUnionDTO.setCodigoRespuestaWU(String.valueOf(datos.getErrNumber()));
    			datosResponseWesterUnionDTO.setTipoDocumento(datos.getCarTypeDocument());
    			datosResponseWesterUnionDTO.setValorPagoSugerido(String.valueOf(datos.getCarValuePaySuggested().doubleValue()));
    			datosResponseWesterUnionDTO.setMensajeRespuestaWU(datos.getErrDescription());
    			datosResponseWesterUnionDTO.setCodigoRespuestaBR(aprobado);
    			datosAdicionales =  datosResponseWesterUnionDTO.getValorTotalPagar() + "|" + datosResponseWesterUnionDTO.getValorminimoPagar() + "|" + datosResponseWesterUnionDTO.getFechaVencimientoPago();
    			
    			String autorizacionBro = null;
				autorizacionBro = String.valueOf(datos.getCarAutorizacion());
				if( autorizacionBro != null){
					if( autorizacionBro.length() <= 6 ){
						try{
							autorizacionBro = ISOUtil.padleft(autorizacionBro, 6, '0');
						}catch(Exception ex){
							log.error(this, ex);
						}//FIN PROCESO AUTORIZACION BRO
					}else{
						autorizacionBro = autorizacionBro.substring(autorizacionBro.length() - 6, autorizacionBro.length());
					}//FIN VALIDACION AUTORIZACION BRO <= 6                                                                                        
				}
				
				datosResponseWesterUnionDTO.setAutorizacionBR(autorizacionBro);
				response.set(27,"1");
				response.set(38,String.valueOf(datosResponseWesterUnionDTO.getAutorizacionBR()));
				response.set(62,String.valueOf(datos.getCarAutorizacion()));
    			response.set(39, datosResponseWesterUnionDTO.getCodigoRespuestaBR());
    			response.set(116, datosResponseWesterUnionDTO.getValorPagar() + "|" + datosResponseWesterUnionDTO.getValorRecargo() + "|" + datosResponseWesterUnionDTO.getNombreCliente() +"|"+ datosAdicionales);
				response.recalcBitMap();
    		}else{
    			datosResponseWesterUnionDTO.setCodigoRespuestaWU(String.valueOf(datos.getErrNumber()));
    			datosResponseWesterUnionDTO.setMensajeRespuestaWU(datos.getErrDescription());
    			datosResponseWesterUnionDTO.setCodigoRespuestaBR(transDeclinada);
    			response.set(27,"1");
    		}
    		
    		actualizaDatosTransaccion(conn2005, datosResponseWesterUnionDTO);
    	}catch(Exception ex){
            log.error(this, ex);
            datosResponseWesterUnionDTO = null;
        }
    	
    	return datosResponseWesterUnionDTO;
    }
    
    private XStream inicializaXstream()throws Exception{
    	XStream xStream 			= null;
    	try{
    		xStream = new XStream(new XppDriver(new XmlFriendlyReplacer("__", "_")){
				public HierarchicalStreamWriter createWriter(Writer out) {
					return new PrettyPrintWriter(out, this.xmlFriendlyReplacer()){
						public void startNode(String name, @SuppressWarnings("rawtypes") Class type) {
							super.startNode(name, type);//                           
						}
					};
				}
			});
    		
    	}catch(Exception e){
    		log.error(this, e);
        }
    	return xStream;
    }
    
    private DatosResponseWesterUnionDTO manejoDatosRespuestaPago(ClsPayResponse datos, InformacionRegistroBaseDTO inforinformacionRegistroBaseDTO) throws Exception{
    	DatosResponseWesterUnionDTO datosResponseWesterUnionDTO = new DatosResponseWesterUnionDTO();
    	SimpleDateFormat sdf 		= new SimpleDateFormat("MM/dd/yyyy hh:mm");
    	String datoRecibo 			= "";
    	try{
    		datosResponseWesterUnionDTO.setIdRegistroActualiza(inforinformacionRegistroBaseDTO.getIdRegistro());
			datosResponseWesterUnionDTO.setSecuenciaBR(String.valueOf(datos.getCarSequential()));
			datosResponseWesterUnionDTO.setSecuenciaSara(String.valueOf(datos.getCarSequentialSARA()));
			datosResponseWesterUnionDTO.setAutorizacionSara(String.valueOf(datos.getCarAutorizacion()));
			datosResponseWesterUnionDTO.setReferenciaConsultaBR(inforinformacionRegistroBaseDTO.getReferenciaConsulta());
			if(datos.isSucess()){
				datosResponseWesterUnionDTO.setFechaSARA(sdf.format(datos.getCarDateSARA().toGregorianCalendar().getTime()));
    			datosResponseWesterUnionDTO.setCuentaCliente(datos.getCarCuenta());
    			datosResponseWesterUnionDTO.setTipoPago(datos.getCarType());
    			datosResponseWesterUnionDTO.setDescripcionPago(datos.getCarTypeDescription());
    			datosResponseWesterUnionDTO.setNombreCliente(datos.getCarNames());
    			datosResponseWesterUnionDTO.setDocumentoCliente(datos.getCarDocumentId());
    			datosResponseWesterUnionDTO.setObservacionPago(datos.getCarObservation());
    			datosResponseWesterUnionDTO.setDireccionObser(datos.getCarAdress());
    			datosResponseWesterUnionDTO.setValorPagar(String.valueOf(datos.getCarValuePaid().doubleValue()));
    			datosResponseWesterUnionDTO.setValorAbonadoCliente(String.valueOf(datos.getCarValueEfective().doubleValue()));
    			datosResponseWesterUnionDTO.setValorTotalPagar(String.valueOf(datos.getCarValueTotal().doubleValue()));
    			datosResponseWesterUnionDTO.setValorminimoPagar(String.valueOf(datos.getCarValueMin().doubleValue()));
    			datosResponseWesterUnionDTO.setValorRecargo(String.valueOf(datos.getCarValueActivaCharge().doubleValue()));
    			datosResponseWesterUnionDTO.setFechaVencimientoPago(sdf.format(datos.getCarDateVen().toGregorianCalendar().getTime()));
    			datosResponseWesterUnionDTO.setNumFactura(datos.getCarFactura());
    			datosResponseWesterUnionDTO.setCodigoRespuestaWU(String.valueOf(datos.getErrNumber()));
    			datosResponseWesterUnionDTO.setMensajeRespuestaWU(datos.getErrDescription());
    			datosResponseWesterUnionDTO.setCodigoRespuestaBR(aprobado);
    			
    			XStream xStream = inicializaXstream();
    			
    			if(!datos.getCarPrintFactura().isEmpty()){
    				
    				datosResponseWesterUnionDTO.setTicketImpresion(datos.getCarPrintFactura());
    				xStream.processAnnotations(new Class[]{XMLFactura.class});//[1] PROCESA TODAS LAS ANOTACIONES 
    				xStream.autodetectAnnotations(true);//[2] HABILITA AUTODETENCION DE CLASES ANOTADAS
    				xStream.alias("Factura", Factura.class);     
    				xStream.alias("Cabecera", Cabecera.class);
    				xStream.alias("Detalles", Detalles.class);
    				xStream.alias("Detalle", Detalle.class);
            		//x.addImplicitCollection(Factura.class, "Detalle", Detalle.class);
    				//XMLFactura xmlFactura = (XMLFactura)xStream.fromXML(datos.getCarPrintFactura());
    				
    			}else if(!datos.getCarPrintRecibo().isEmpty()){
    				
    				datosResponseWesterUnionDTO.setTicketImpresion(datos.getCarPrintRecibo());
    				xStream.processAnnotations(new Class[]{Recibo.class});//[1] PROCESA TODAS LAS ANOTACIONES 
    				xStream.autodetectAnnotations(true);//[2] HABILITA AUTODETENCION DE CLASES ANOTADAS
    				xStream.alias("Recibo", Recibo.class);     
    				xStream.alias("Cabecera", Cabecera.class);
    				xStream.alias("Detalle", com.westermunion.XMLRecibo.Detalle.class);
    				log.info(xStream.fromXML(datos.getCarPrintRecibo()));
    				Recibo recibo = (Recibo)xStream.fromXML(datos.getCarPrintRecibo());
    				datoRecibo = armaDatosEnvioRecibo(recibo);
    				log.info(" String Output : " + datoRecibo);
    				datosResponseWesterUnionDTO.setTicketISOimpresion(datoRecibo);
            		
    			}else if(!datos.getCarPrintOtros().isEmpty()){
    				
    				datosResponseWesterUnionDTO.setTicketImpresion(datos.getCarPrintOtros());
					
    				if(datos.getCarPrintOtros().contains("Enterprise")){
    					
    					log.info(" XML Output : " + datos.getCarPrintOtros());
    					xStream.processAnnotations(new Class[]{ReciboEnterprise.class});//[1] PROCESA TODAS LAS ANOTACIONES 
    					xStream.autodetectAnnotations(true);//[2] HABILITA AUTODETENCION DE CLASES ANOTADAS
    					xStream.alias("ReciboEnterprise", ReciboEnterprise.class); 
                		log.info(xStream.fromXML(datos.getCarPrintOtros()));
                		ReciboEnterprise resultado = (ReciboEnterprise)xStream.fromXML(datos.getCarPrintOtros().toString());
                		datoRecibo = armaDatosEnvioReciboEnterprise(resultado);
                		log.info(" String Output : " + datoRecibo);
                		datosResponseWesterUnionDTO.setTicketISOimpresion(datoRecibo);
                		
                	}else{
    				
    					//XMLOtrosRecibo XMLOtrosRecibo = new XMLOtrosRecibo();
    					xStream.processAnnotations(new Class[]{XMLOtrosRecibo.class});//[1] PROCESA TODAS LAS ANOTACIONES 
    					xStream.autodetectAnnotations(true);//[2] HABILITA AUTODETENCION DE CLASES ANOTADAS
    					xStream.alias("Recibo", com.westermunion.XMLOtrosRecibos.Recibo.class);     
    					xStream.alias("Cabecera", com.westermunion.XMLOtrosRecibos.Cabecera.class);
    					xStream.alias("Detalle", com.westermunion.XMLOtrosRecibos.Detalle.class);
                		//x.addImplicitCollection(XMLRecibo.class, "Detalle", com.westermunion.XMLOtrosRecibos.Detalle.class);
    					//XMLOtrosRecibo XMLOtrosRecibo = (XMLOtrosRecibo)xStream.fromXML(datos.getCarPrintOtros());
    				
    				}
    				
    				
    			}
    			setearTicketISO(datoRecibo);
    			String autorizacionBro = null;
				autorizacionBro = String.valueOf(datos.getCarAutorizacion());
				if( autorizacionBro != null){
					if( autorizacionBro.length() <= 6 ){
						try{
							autorizacionBro = ISOUtil.padleft(autorizacionBro, 6, '0');
						}catch(Exception ex){
							log.error(this, ex);
						}//FIN PROCESO AUTORIZACION BRO
					}else{
						autorizacionBro = autorizacionBro.substring(autorizacionBro.length() - 6, autorizacionBro.length());
					}//FIN VALIDACION AUTORIZACION BRO <= 6                                                                                        
				}
				
				datosResponseWesterUnionDTO.setAutorizacionBR(autorizacionBro);
				
				response.set(27,"1");
				response.set(38,String.valueOf(datosResponseWesterUnionDTO.getAutorizacionBR()));
				response.set(62,String.valueOf(datos.getCarAutorizacion()));
    			response.recalcBitMap();
    		}else{
				datosResponseWesterUnionDTO.setCodigoRespuestaWU(String.valueOf(datos.getErrNumber()));
    			datosResponseWesterUnionDTO.setMensajeRespuestaWU(datos.getErrDescription());
    			datosResponseWesterUnionDTO.setCodigoRespuestaBR(transDeclinada);
    			response.set(27,"1");
			}
			actualizaDatosTransaccion(conn2005, datosResponseWesterUnionDTO);
    	}catch(Exception e){
    		log.error(this, e);
            datosResponseWesterUnionDTO = null;
    	}
    	return datosResponseWesterUnionDTO;
    }
    
    private DatosResponseWesterUnionDTO manejoDatosRespuestaReverso(ClsReverseResponse datos, InformacionRegistroBaseDTO inforinformacionRegistroBaseDTO) throws Exception{
    	DatosResponseWesterUnionDTO datosResponseWesterUnionDTO = new DatosResponseWesterUnionDTO();
    	SimpleDateFormat sdf 		= new SimpleDateFormat("MM/dd/yyyy hh:mm");
    	try{
    		datosResponseWesterUnionDTO.setIdRegistroActualiza(inforinformacionRegistroBaseDTO.getIdRegistro());
			datosResponseWesterUnionDTO.setSecuenciaBR(String.valueOf(datos.getCarSequential()));
			datosResponseWesterUnionDTO.setSecuenciaSara(String.valueOf(datos.getCarSequentialSARA()));
			datosResponseWesterUnionDTO.setAutorizacionSara(String.valueOf(datos.getCarAutorizacion()));
			datosResponseWesterUnionDTO.setReferenciaConsultaBR(inforinformacionRegistroBaseDTO.getReferenciaConsulta());
			if(datos.isSucess()){
				datosResponseWesterUnionDTO.setFechaSARA(sdf.format(datos.getCarDateSARA().toGregorianCalendar().getTime()));
    			datosResponseWesterUnionDTO.setCuentaCliente(datos.getCarCuenta());
    			datosResponseWesterUnionDTO.setTipoPago(datos.getCarType());
    			datosResponseWesterUnionDTO.setDescripcionPago(datos.getCarTypeDescription());
    			datosResponseWesterUnionDTO.setNombreCliente(datos.getCarNames());
    			datosResponseWesterUnionDTO.setDocumentoCliente(datos.getCarDocumentId());
    			datosResponseWesterUnionDTO.setObservacionPago(datos.getCarObservation());
    			datosResponseWesterUnionDTO.setDireccionObser(datos.getCarAdress());
    			datosResponseWesterUnionDTO.setValorPagar(String.valueOf(datos.getCarValuePaid().doubleValue()));
    			datosResponseWesterUnionDTO.setValorAbonadoCliente(String.valueOf(datos.getCarValueEfective().doubleValue()));
    			datosResponseWesterUnionDTO.setValorTotalPagar(String.valueOf(datos.getCarValueTotal().doubleValue()));
    			datosResponseWesterUnionDTO.setValorminimoPagar(String.valueOf(datos.getCarValueMin().doubleValue()));
    			datosResponseWesterUnionDTO.setValorRecargo(String.valueOf(datos.getCarValueActivaCharge().doubleValue()));
    			datosResponseWesterUnionDTO.setFechaVencimientoPago(sdf.format(datos.getCarDateVen().toGregorianCalendar().getTime()));
    			datosResponseWesterUnionDTO.setNumFactura(datos.getCarFactura());
    			datosResponseWesterUnionDTO.setCodigoRespuestaWU(String.valueOf(datos.getErrNumber()));
    			datosResponseWesterUnionDTO.setMensajeRespuestaWU(datos.getErrDescription());
    			datosResponseWesterUnionDTO.setCodigoRespuestaBR(aprobado);
    			
    			String autorizacionBro = null;
				autorizacionBro = String.valueOf(datos.getCarAutorizacion());
				if( autorizacionBro != null){
					if( autorizacionBro.length() <= 6 ){
						try{
							autorizacionBro = ISOUtil.padleft(autorizacionBro, 6, '0');
						}catch(Exception ex){
							log.error(this, ex);
						}//FIN PROCESO AUTORIZACION BRO
					}else{
						autorizacionBro = autorizacionBro.substring(autorizacionBro.length() - 6, autorizacionBro.length());
					}//FIN VALIDACION AUTORIZACION BRO <= 6                                                                                        
				}
				
				datosResponseWesterUnionDTO.setAutorizacionBR(autorizacionBro);
				response.set(27,"1");
				response.set(38,String.valueOf(datosResponseWesterUnionDTO.getAutorizacionBR()));
				response.set(62,String.valueOf(datos.getCarAutorizacion()));
				response.recalcBitMap();
    		}else{
				datosResponseWesterUnionDTO.setCodigoRespuestaWU(String.valueOf(datos.getErrNumber()));
    			datosResponseWesterUnionDTO.setMensajeRespuestaWU(datos.getErrDescription());
    			datosResponseWesterUnionDTO.setCodigoRespuestaBR(transDeclinada);
    			response.set(27,"1");
			}
			actualizaDatosTransaccion(conn2005, datosResponseWesterUnionDTO);
    	}catch(Exception e){
    		log.error(this, e);
            datosResponseWesterUnionDTO = null;
    	}
    	return datosResponseWesterUnionDTO;
    }
    
    public JSONArray SeparaDatosCampo117(String temporal117, String proceso)throws Exception{
 	   JSONArray datosCampo117 = new JSONArray();
 	   try{
 		   if(proceso.equals("CON")){
 			  //CONSULTA
 			   if(temporal117.length()>0){
 				   JSONObject dato = new JSONObject();
 				   
 				   	//NOMBRE-CONTRA-PARTIDA - 50 ESPACIOS 0-50
                    if (temporal117.substring(0,50).trim().compareTo("")!=0){
                 	   dato.put("nombreContraPartida", temporal117.substring(0,50).trim());
                    }else{
                 	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (NOMBRE-CONTRA-PARTIDA  0-50)");
                 	   dato.put("nombreContraPartida", "");
                    }
                    //CEDULA - 15 ESPACIOS 50-65
                    if (temporal117.substring(50,65).trim().compareTo("")!=0){
                  	   dato.put("cedula", temporal117.substring(50,65).trim());
                    }else{
                  	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (CEDULA  50-65)");
                  	   dato.put("cedula", "");
                    }
                    //DIRECCION - 50 ESPACIOS 65-115
                    if (temporal117.substring(65,115).trim().compareTo("")!=0){
                   	   dato.put("direccion", temporal117.substring(65,115).trim());
                    }else{
                   	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (DIRECCION  65-115)");
                   	   dato.put("direccion", "");
                    }
                    //TELEFONO - 15 ESPACIOS 115-130
                    if (temporal117.substring(115,130).trim().compareTo("")!=0){
                	   dato.put("telefono", temporal117.substring(115,130).trim());
                     }else{
                	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (TELEFONO  115-130)");
                	   dato.put("telefono", "");
                    }
                    //REFERENCIA - 25 ESPACIOS 130-155
                    if (temporal117.substring(130,155).trim().compareTo("")!=0){
                 	   dato.put("referencia", temporal117.substring(130,155).trim());
                    }else{
                 	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (REFERENCIA  130-155)");
                 	   dato.put("referencia", "");
                    }
                    //REFERENCIA-ADICIONAL - 5 ESPACIOS 155-160
                    if (temporal117.substring(155,160).trim().compareTo("")!=0){
                 	   dato.put("referencia_Ad", temporal117.substring(155,160).trim());
                 	   dato.put("referenciaAd" , temporal117.substring(155,160).trim());
                    }else{
                 	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (REFERENCIA-ADICIONAL  155-160))");
                 	   dato.put("referencia_Ad", "");
                 	   dato.put("referenciaAd" , "");
                    }
                    //CIUDAD-CLIENTE - 5 ESPACIOS 160-165 
                    if (temporal117.substring(160,165).trim().compareTo("")!=0){
                 	   dato.put("ciudadCliente", temporal117.substring(160,165).trim());
                    }else{
                 	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (CIUDAD-CLIENTE  160-165))");
                 	   dato.put("ciudadCliente", "");
                    }
                    //SPACE DE 10 - 165-175
                    if (temporal117.substring(165,175).trim().compareTo("")!=0){
                 	   dato.put("space", temporal117.substring(165,175).trim());
                    }else{
                 	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (SPACE  165-175))");
                 	   dato.put("space", "");
                    }
                    //CORREODTV 50 ESPACIOS - 175-225
                    /*if (temporal117.substring(175,225).trim().compareTo("")!=0){
                 	   dato.put("correoDTV", temporal117.substring(175,225).trim());
                    }else{
                 	   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117 - (CORREODTV  175-225))");
                 	   dato.put("correoDTV", "");
                    }*/
 				   
                    datosCampo117.put(dato);
 			   }else{
 				   log.info("NO SE HAN ENVIADO DATOS EN CAMPO 117");
 			   }
 		   }
 	   }catch(Exception ex){
 	          log.error(this, ex);
 	   }
 	   return datosCampo117; 
    }
	
    private String armaDatosEnvioReciboEnterprise(ReciboEnterprise reciboEnterprise) throws Exception{
    	String datoArmado ="| ";
    	try{
    		if(reciboEnterprise.getFecha()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getFecha().toString();
    		}
    		if(reciboEnterprise.getTitulo()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getTitulo().toString();
    		}
    		if(reciboEnterprise.getEntidad()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getEntidad().toString();
    		}
    		if(reciboEnterprise.getCEP()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getCEP().toString();
    		}
    		if(reciboEnterprise.getCUENTA()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getCUENTA().toString();
    		}
    		if(reciboEnterprise.getNo_TRAMITE_PLACA()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getNo_TRAMITE_PLACA().toString();
    		}
    		if(reciboEnterprise.getNOMBRES()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getNOMBRES().toString();
    		}
    		if(reciboEnterprise.getUSUARIO()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getUSUARIO().toString();
    		}
    		if(reciboEnterprise.getCED_RUC_PAS()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getCED_RUC_PAS().toString();
    		}
    		if(reciboEnterprise.getOBSERV()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getOBSERV().toString();
    		}
    		if(reciboEnterprise.getDIRECCION()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getDIRECCION().toString();
    		}
    		if(reciboEnterprise.getAUT__TRAMITE()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getAUT__TRAMITE().toString();
    		}
    		if(reciboEnterprise.getDivision()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getDivision().toString();
    		}else{
    			datoArmado = datoArmado+" |";
    		}
    		if(reciboEnterprise.getValor_Factura()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getValor_Factura().toString();
    		}
    		if(reciboEnterprise.getValor_Recaudado()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getValor_Recaudado().toString();
    		}
    		if(reciboEnterprise.getCargo_Activa()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getCargo_Activa().toString();
    		}
    		if(reciboEnterprise.getValor_Total()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getValor_Total().toString();
    		}
    		if(reciboEnterprise.getTotal_Recaudado()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getTotal_Recaudado().toString();
    		}
    		if(reciboEnterprise.getSaldo()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getSaldo().toString();
    		}
    		if(reciboEnterprise.getAgencia()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getAgencia().toString();
    		}
    		if(reciboEnterprise.getOperador()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getOperador().toString();
    		}
    		if(reciboEnterprise.getValidacion()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getValidacion().toString();
    		}
    		if(reciboEnterprise.getCodigo_Activa()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getCodigo_Activa().toString();
    		}
    		if(reciboEnterprise.getCltid()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getCltid().toString();
    		}
    		if(reciboEnterprise.getValidacion()!=null){
    			datoArmado = datoArmado+"|"+reciboEnterprise.getValidacion().toString();
    		}
    	}catch(Exception e){
    		log.error(this, e);
        }
    	datoArmado = datoArmado + "|";
    	return datoArmado;
    }
    
    private String armaDetalleReciboSRITransaferencia(com.westermunion.XMLRecibo.Detalle detalle) throws Exception{
    	String datoDetalle = " | ";
    	try{
    		if(detalle.getRecaudacion()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getRecaudacion().toString();
    		}
    		if(detalle.getNroPlaca()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getNroPlaca().toString();
    		}
    		if(detalle.getChasis()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getChasis().toString();
    		}
    		if(detalle.getFormapago()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getFormapago().toString();
    		}
    		if(detalle.getCamponombre()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getCamponombre().toString();
    		}
    		if(detalle.getCamponombre2()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getCamponombre2().toString();
    		}
    		if(detalle.getNombres()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getNombres().toString();
    		}
    		if(detalle.getCampocedula()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getCampocedula().toString();
    		}
    		if(detalle.getIdentificacion()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getIdentificacion().toString();
    		}
    		if(detalle.getAvaluo()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getAvaluo().toString();
    		}
    		if(detalle.getContrato()!=null){
    			datoDetalle = datoDetalle+"|"+detalle.getContrato().toString();
    		}
    		datoDetalle = datoDetalle + " | ";
    	}catch(Exception e){
    		log.error(this, e);
        }
    	return datoDetalle;
    }
    
    private String armaDatosEnvioRecibo(Recibo recibo) throws Exception{
    	String datoRecibo = "";
    	try{
    		//---------CABECERA RECIBO------------------------------------------------
    		String datocabecera ="| ";
    		if(recibo.getCabecera().getTitulo()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getTitulo().toString();
    		}
    		if(recibo.getCabecera().getSubTitulo()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getSubTitulo().toString();
    		}
    		if(recibo.getCabecera().getEmpresa()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getEmpresa().toString();
    		}
    		if(recibo.getCabecera().getRUC()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getRUC().toString();
    		}
    		if(recibo.getCabecera().getOficina()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getOficina().toString();
    		}
    		if(recibo.getCabecera().getDireccion()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getDireccion().toString();
    		}
    		if(recibo.getCabecera().getComprobante()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getComprobante().toString();
    		}
    		if(recibo.getCabecera().getCodigoActiva()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getCodigoActiva().toString();
    		}
    		if(recibo.getCabecera().getCodigo()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getCodigo().toString();
    		}
    		if(recibo.getCabecera().getFecha()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getFecha().toString();
    		}
    		if(recibo.getCabecera().getCategoria()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getCategoria().toString();
    		}
    		if(recibo.getCabecera().getFecha_de_Evento()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getFecha_de_Evento().toString();
    		}
    		if(recibo.getCabecera().getDocumento()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getDocumento().toString();
    		}
    		if(recibo.getCabecera().getNombres()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getNombres().toString();
    		}
    		if(recibo.getCabecera().getEdad()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getEdad().toString();
    		}
    		if(recibo.getCabecera().getTipo_Discapacidad()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getTipo_Discapacidad().toString();
    		}
    		if(recibo.getCabecera().getTalla()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getTalla().toString();
    		}
    		if(recibo.getCabecera().getCuenta()!=null){
    			datocabecera = datocabecera+"|"+recibo.getCabecera().getCuenta().toString();
    		}
    		datocabecera = datocabecera+" | ";
    		datoRecibo = datoRecibo + datocabecera;
    		//-------------------------------------------------------------------------------------
    		//--------------------------DETALLE RECIBO---------------------------------------------
    		String datoDetalle = "";
    		if(this.codEmpresaWU=="0000"){//JUDICATURA
	    		if(recibo.getDetalle().getEspacio1()!=null){
	    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getEspacio1().toString();
	    		}else{
	    			datoDetalle = datoDetalle+" | ";
	    		}
    		}
    		if(recibo.getDetalle().getComprobante()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getComprobante().toString();
    		}
    		if(recibo.getDetalle().getRecaudacion()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getRecaudacion().toString();
    		}
    		if(this.codEmpresaWU=="0000"){//SRI
    			if(recibo.getDetalle().getAutSri()!=null && recibo.getDetalle().getComprobante().contains("RISE")){
        			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getAutSri().toString();
        		}
    			if(recibo.getDetalle().getAutsri()!=null && recibo.getDetalle().getComprobante().contains("MATRICULA")){
        			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getAutsri().toString();
        		}
    		}
    		if(this.codEmpresaWU=="0000"){//JUDICATURA
	    		if(recibo.getDetalle().getEspacio2()!=null){
	    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getEspacio2().toString();
	    		}else{
	    			datoDetalle = datoDetalle+" | ";
	    		}
    		}
    		//----------------VALIDAR ANT-  --------------------------------
    		if(recibo.getDetalle().getIdentificacion()!=null && this.codEmpresaWU=="0000"){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getIdentificacion().toString();
    		}
    		//------------------------------------------------------------
    		if(recibo.getDetalle().getCamponombre()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCamponombre().toString();
    		}
    		if(recibo.getDetalle().getNombres()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getNombres().toString();
    		}
    		if(recibo.getDetalle().getCI()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCI().toString();
    		}
    		if(recibo.getDetalle().getTipoIdentificacion()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getTipoIdentificacion().toString();
    		}
    		if(recibo.getDetalle().getIdentificacion()!=null && this.codEmpresaWU!="0000"){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getIdentificacion().toString();
    		}
    		if(recibo.getDetalle().getCodigo()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCodigo().toString();
    		}
    		if(this.codEmpresaWU=="0000"){//JUDICATURA
	    		if(recibo.getDetalle().getEspacio3()!=null ){
	    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getEspacio3().toString();
	    		}else{
	    			datoDetalle = datoDetalle+" | ";
	    		}
    		}
    		if(recibo.getDetalle().getTipopago()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getTipopago().toString();
    		}
    		if(recibo.getDetalle().getOperacionBanco()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getOperacionBanco().toString();
    		}
    		if(recibo.getDetalle().getSolicitud()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getSolicitud().toString();
    		}
    		if(recibo.getDetalle().getNroPlaca()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getNroPlaca().toString();
    		}
    		if(recibo.getDetalle().getPlaca()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getPlaca().toString();
    		}
    		if(recibo.getDetalle().getAvaluo()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getAvaluo().toString();
    		}
    		if(recibo.getDetalle().getChasis()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getChasis().toString();
    		}
    		if(recibo.getDetalle().getRubro()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getRubro().toString();
    		}
    		if(recibo.getDetalle().getCampoconceptopago()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCampoconceptopago().toString();
    		}
    		if(recibo.getDetalle().getConceptopago()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getConceptopago().toString();
    		}
    		if(recibo.getDetalle().getCepNro()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCepNro().toString();
    		}
    		if(recibo.getDetalle().getRucTransaccion()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getRucTransaccion().toString();
    		}
    		if(recibo.getDetalle().getImpuesto()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getImpuesto().toString();
    		}
    		if(this.codEmpresaWU!="0000"){//SRI
	    		if(recibo.getDetalle().getAutSri()!=null){
	    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getAutSri().toString();
	    		}
    		}else if(recibo.getDetalle().getComprobante().contains("CEP")){
    			if(recibo.getDetalle().getAutSri()!=null){
	    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getAutSri().toString();
	    		}
    		}
    		if(recibo.getDetalle().getNombreAlimentado()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getNombreAlimentado().toString();
    		}
    		if(recibo.getDetalle().getNombreAlimentadoDes()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getNombreAlimentadoDes().toString();
    		}
    		if(recibo.getDetalle().getIdAlimentado()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getIdAlimentado().toString();
    		}
    		if(recibo.getDetalle().getNumPago()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getNumPago().toString();
    		}
    		if(recibo.getDetalle().getPeriodo()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getPeriodo().toString();
    		}
    		if(recibo.getDetalle().getFormapago()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getFormapago().toString();
    		}
    		if(recibo.getDetalle().getFisco()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getFisco().toString();
    		}
    		if(recibo.getDetalle().getCte()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCte().toString();
    		}
    		if(recibo.getDetalle().getImpuestorodaje()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getImpuestorodaje().toString();
    		}
    		if(recibo.getDetalle().getJunta()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getJunta().toString();
    		}
    		if(recibo.getDetalle().getCpguayas()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getCpguayas().toString();
    		}
    		if(recibo.getDetalle().getImpanbiental()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getImpanbiental().toString();
    		}
    		if(recibo.getDetalle().getBrigada()!=null){
    			datoDetalle = datoDetalle+"|"+recibo.getDetalle().getBrigada().toString();
    		}
    		datoDetalle = datoDetalle+" | ";
    		if(recibo.getDetalle().getRecaudacion().contains("DOMINIO") && this.codEmpresaWU!="0000"){//SRI TRANSFERENCIA DE DOMINIO
    			
    			datoRecibo = datoRecibo + armaDetalleReciboSRITransaferencia(recibo.getDetalle());;
    		}else{
    			datoRecibo = datoRecibo + datoDetalle;
    		}
    		//-------------------------------------------------------------------------------------
    		//--------------------------TOTALES----------------------------------------------------
    		if(recibo.getValorRecaudado()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getValorRecaudado().toString();
    		}
    		if(recibo.getComision()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getComision().toString();
    		}
    		if(recibo.getComisionBase()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getComisionBase().toString();
    		}
    		if(recibo.getComisionIVA()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getComisionIVA().toString();
    		}
    		if(recibo.getCompensacionIVA()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getCompensacionIVA().toString();
    		}
    		if(recibo.getValorTotal()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getValorTotal().toString();
    		}
    		if(recibo.getTotal()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getTotal().toString();
    		}
    		if(recibo.getAgente()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getAgente().toString();
    		}
    		if(recibo.getReferencia()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getReferencia().toString();
    		}
    		if(recibo.getCiudad()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getCiudad().toString();
    		}
    		if(recibo.getUsuario()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getUsuario().toString();
    		}
    		if(recibo.getMensaje()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getMensaje().toString();
    		}
    		if(recibo.getValidacion()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getValidacion().toString();
    		}
    		if(recibo.getSeparador()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getSeparador().toString();
    		}
    		if(recibo.getOriginal()!=null){
    			datoRecibo = datoRecibo+"|"+recibo.getOriginal().toString();
    		}
    		datoRecibo = datoRecibo + " |";
    		//-------------------------------------------------------------------------------------
    		
    	}catch(Exception e){
    		log.error(this, e);
        }
    	return datoRecibo;
    }
    
    private void setearTicketISO(String datoRecibo) throws Exception{
    	String[] datosISO = new String[4];
    	try{
    		int datoControl = 0;
    		int numCaracteres = datoRecibo.length();
    		int numIteraciones = (numCaracteres/numeroMaxDato)+1;
    		for(int i=0;i<numIteraciones;i++){
    			if(i>3){
    				break;
    			}
	    		if(numCaracteres>numeroMaxDato){
	    			datosISO[i] = datoRecibo.substring(datoControl, (datoControl+(numeroMaxDato-1)));
	    			datoControl = datoControl + numeroMaxDato;
	    			numCaracteres = numCaracteres - numeroMaxDato;
	    		}else{
	    			datosISO[i] = datoRecibo.substring(datoControl);
	    			break;
	    		}
    		}
    		for(int i=0;i<datosISO.length;i++){
    			log.info(datosISO[i]);
    			if(datosISO[i].length()>0){
    				response.set((119+i), datosISO[i]);
    			}else{
    				break;
    			}
    		}
    	}catch(Exception e){
    		log.error(this, e);
        }
    }
    
    @Override
    public void run() {
    	InformacionRegistroBaseDTO informacionRegistroBaseDTO = new InformacionRegistroBaseDTO();
    	try {
    		if(obtenerConexiones(connPool2005 )  && ServerProcessListener.flagBaseBloqueo == false){
    			Servicios 		cliente 	= null;
    			ServiciosSoap 	cone 		= null;
    	            try{
    	                cliente = new Servicios(); 
    	                cliente.getWSDLDocumentLocation().openConnection().setConnectTimeout((30*1000));
    	                cone = cliente.getServiciosSoap();
    	            }catch(Exception ex){
    	                log.error(this, ex);
    	                cone = null;
    	            }//FIN PROCESO SETEO CLIENTE DATOS
    	            
    	            if(cone != null){
    	            	//METODO DE HOMOLOGACION DE CODIGOS EMPRESA CNEL
    	            	if(this.codEmpresaWU == "CNL"){
    	            		homologaCodigoCNEL();
    	            	}
    	            	informacionRegistroBaseDTO = (InformacionRegistroBaseDTO)guardaDatosInicial(conn2005,request,"","",""); 
    	            	//CONSULTA
    	            	if (request.getString(0).compareTo("0200") == 0 && (request.getString(3).compareTo("280000")==0 || request.getString(3).compareTo("300000")==0) && request.getString(4).compareTo("000000000000")==0){
    	            		ClsConsultRequest datosConsulta = new ClsConsultRequest();
    	            		if(informacionRegistroBaseDTO != null){
    	            			datosConsulta = armaDatosConsulta(informacionRegistroBaseDTO);
    	            			ClsConsultResponse datosRespuestaConsulta = cone.queryCollection(datosConsulta);
    	            			if(datosRespuestaConsulta != null){
        	            			DatosResponseWesterUnionDTO datosResponseWesterUnionDTO = manejoDatosRespuestaConsulta(datosRespuestaConsulta);
        	            			if(datosResponseWesterUnionDTO!=null){
        	            				responseServer(datosResponseWesterUnionDTO.getCodigoRespuestaBR());
        	            			}else{
        	            				responseServer(errorTransaccion);  
        	            			}
        	            		}else{
        	            			responseServer(errorTransaccion);  
        	            		}
    	            		}else{
    	            			responseServer(errorTransaccion);  
    	            		}
    	            	}
    	            	//PAGO
    	            	if (request.getString(0).compareTo("0200") == 0 && (request.getString(3).compareTo("280000")==0 || request.getString(3).compareTo("300000")==0) && request.getString(4).compareTo("000000000000")!=0){
    	            		ClsPayRequest datosPago = new ClsPayRequest();
    	            		if(informacionRegistroBaseDTO != null){
    	            			datosPago = armaDatosPago(informacionRegistroBaseDTO);
    	            			ClsPayResponse datosRespuestaPago = cone.saveCollection(datosPago);
    	            			if(datosRespuestaPago !=null){
    	            				DatosResponseWesterUnionDTO datosResponseWesterUnionDTO = manejoDatosRespuestaPago(datosRespuestaPago,informacionRegistroBaseDTO);
    	            				if(datosResponseWesterUnionDTO!=null){
        	            				responseServer(datosResponseWesterUnionDTO.getCodigoRespuestaBR());
        	            			}else{
        	            				responseServer(errorTransaccion);  
        	            			}
    	            			}else{
        	            			responseServer(errorTransaccion);  
        	            		}
    	            		}else{
    	            			responseServer(errorTransaccion);  
    	            		}
    	            	}
    	            	//REVERSO
    	            	if (request.getString(0).compareTo("0400") == 0 && (request.getString(3).compareTo("280000")==0 || request.getString(3).compareTo("300000")==0) && request.getString(4).compareTo("000000000000")!=0){
    	            		ClsReverseRequest datosReverso = new ClsReverseRequest();
    	            		if(informacionRegistroBaseDTO != null){
    	            			datosReverso = armaDatosReverso(informacionRegistroBaseDTO);
    	            			ClsReverseResponse datosRespuestaReverso = cone.reverseCollection(datosReverso);
    	            			if(datosRespuestaReverso !=null){
    	            				DatosResponseWesterUnionDTO datosResponseWesterUnionDTO = manejoDatosRespuestaReverso(datosRespuestaReverso,informacionRegistroBaseDTO);
    	            				if(datosResponseWesterUnionDTO!=null){
        	            				responseServer(datosResponseWesterUnionDTO.getCodigoRespuestaBR());
        	            			}else{
        	            				responseServer(errorTransaccion);  
        	            			}
    	            			}else{
        	            			responseServer(errorTransaccion);  
        	            		}
    	            		}else{
    	            			responseServer(errorTransaccion);  
    	            		}
    	            	}
    	            }else{
    	            	responseServer(errorBase);    
    	            }
    		}else{//SI NO OBTENGO CONEXION DE BASE DE DATOS 28               
    			responseServer(errorBase);              
    		}//FIN VALIDACION OBTENER CONEXION Y SEVER PROCESS LISTENER 
    		cerrarConexionBaseDatos(request);
    	}catch (SQLException e) {
    		// TODO Auto-generated catch block
    		log.error(this, e);
    		e.printStackTrace();
    	}catch (Exception e) {
    		// TODO Auto-generated catch block
    		log.error(this, e);
    		e.printStackTrace();
    	}
    }
    
    public InformacionRegistroBaseDTO  guardaDatosInicial(Connection connection, ISOMsg request, String mensajeResp, String codigoResp, String idTransaccion) throws Exception{
    	String spName 			= "sp_log_westermUnion_v1";
		String spEjecutar 		= null;
		CallableStatement cs 	= null;
		String tipoTransaccion	= "";
		String codigoProceso 	= "";
		String monto 			= "";
		String referenciaBR 	= "";
		String horaTrn 			= "";
		String fechaTrn 		= "";
		String terminalBR 		= "";
		String codigoISO 		= "";
		String contrapartidaWU  = "";
		String codProBR			= "";
		InformacionRegistroBaseDTO informacionRegistroBaseDTO = null;
    	try{
    		
    		spEjecutar 				= "{call " 	+ spName + "( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? "
    				                                     + ", ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? )}";
    		cs 						= connection.prepareCall(spEjecutar);
    		cs.setQueryTimeout(5);
    		//MTI
    		if(request.hasField(0)){
    			tipoTransaccion = request.getString(0);
    			cs.setString(1, tipoTransaccion);
			}else{
    			cs.setString(1, tipoTransaccion);
			}
    		//COD PROCESO
    		if(request.hasField(3)){
    			codigoProceso = request.getString(3);
    			cs.setString(2, codigoProceso);
			}else{
    			cs.setString(2, codigoProceso);
			}
    		//MONTO
    		if(request.hasField(4)){
    			monto = request.getString(4);
    			cs.setString(3, ISOUtil.padleft(monto, 12, '0'));
    		}else{
    			cs.setString(3, monto);
    		}
    		//REFERENCIA
    		if(request.hasField(11)){
    			referenciaBR = request.getString(11);
    			cs.setString(4, referenciaBR);
    		}else{
    			cs.setString(4, referenciaBR);
    		}
    		//HORA
    		if(request.hasField(12)){
    			horaTrn = request.getString(12);
    			cs.setString(5, horaTrn);
			}else{
    			cs.setString(5, horaTrn);
			}
    		//FECHA MMDD
    		if(request.hasField(13)){
    			fechaTrn = request.getString(13);
    			cs.setString(6, fechaTrn);
			}else{
    			cs.setString(6, fechaTrn);
			}
    		//AUTORIZACION
    		cs.setString(7, "");
    		//TERMINAL
    		if(request.hasField(41)){
    			terminalBR = request.getString(41);
    			cs.setString(8, terminalBR);
			}else{
    			cs.setString(8, terminalBR);
			}
    		//CODIGO ISO
    		if(request.hasField(42)){
    			codigoISO = request.getString(42);
    			cs.setString(9, codigoISO);
			}else{
    			cs.setString(9, codigoISO);
			}
    		//CODIGO RESPUESTA BR
    		cs.setString(10, "");
    		//CODIGO PROVEEDOR WU
    		cs.setString(11, this.codEmpresaWU);
    		//CONTRAPARTIDA
    		if(request.hasField(112)){
    			contrapartidaWU = request.getString(112).substring(4).toString().trim();
    			cs.setString(12, contrapartidaWU);
			}else{
    			cs.setString(12, contrapartidaWU);
			}
    		//TIPO SERVICIO
    		cs.setString(13, "");
    		//DESCRIPCION SERVICIO
    		cs.setString(14, "");
    		//DOCUMENTO CLIENTE
    		cs.setString(15, "");
    		//NOMBRE CLIENTE
    		cs.setString(16, "");
    		//TIPO IDENTIFICACION CLIENTE
    		cs.setString(17, "");
    		//18 CODIGO PROVEEDOR BROADNET
    		if(request.hasField(112)){
    			codProBR = request.getString(112).substring(0,2).toString().trim();
    			cs.setString(18, codProBR);
			}else{
    			cs.setString(18, codProBR);
			}
    		//19 NOMBRE PROVEEDOR BROADNET
    		cs.setString(19, this.Empresa);
			
    		// Parametro Salida
    		cs.registerOutParameter(20, Types.VARCHAR);//id registro                     
    		cs.registerOutParameter(21, Types.VARCHAR);//id reverso                     
    		cs.registerOutParameter(22, Types.VARCHAR);//tipo servicio                     
    		cs.registerOutParameter(23, Types.VARCHAR);//descripcion servicios                     
    		cs.registerOutParameter(24, Types.VARCHAR);//referencia consulta                     
    		cs.registerOutParameter(25, Types.VARCHAR);//secuencial SARA                     
    		cs.registerOutParameter(26, Types.VARCHAR);//numero documento cliente                     
    		cs.registerOutParameter(27, Types.VARCHAR);//nombre cliente 
    		cs.registerOutParameter(28, Types.VARCHAR);//tipo documento cliente 
    		cs.registerOutParameter(29, Types.VARCHAR);//valor total pagar 
    		cs.registerOutParameter(30, Types.VARCHAR);//recargo 
    		cs.registerOutParameter(31, Types.VARCHAR);//secuencial broadnet 
    		cs.registerOutParameter(32, Types.VARCHAR);//autorizacion SARA
    		
    		cs.execute();
    		
    		informacionRegistroBaseDTO = new InformacionRegistroBaseDTO();
    		informacionRegistroBaseDTO.setIdRegistro(cs.getString(20));
    		informacionRegistroBaseDTO.setIdReverso(cs.getString(21));
    		informacionRegistroBaseDTO.setTipoServicio(cs.getString(22));
    		informacionRegistroBaseDTO.setDescripcionServicio(cs.getString(23));
    		informacionRegistroBaseDTO.setReferenciaConsulta(cs.getString(24));
    		informacionRegistroBaseDTO.setSecuencialSARA(cs.getString(25));
    		informacionRegistroBaseDTO.setNumeroDocumentoCliente(cs.getString(26));
    		informacionRegistroBaseDTO.setNombreCliente(cs.getString(27));
    		informacionRegistroBaseDTO.setTipoDocumentoCliente(cs.getString(28));
    		informacionRegistroBaseDTO.setValorTotalPagar(cs.getString(29));
    		informacionRegistroBaseDTO.setValorComision(cs.getString(30));
    		informacionRegistroBaseDTO.setSecuencialBR(cs.getString(31));
    		informacionRegistroBaseDTO.setAutorizacionSARA(cs.getString(32));
    		
    		cs.close();
    		
    	}catch(Exception e){
    		log.error(this, e);
    		e.printStackTrace();
    	}
    	return informacionRegistroBaseDTO;
    }    

    public void  actualizaDatosTransaccion(Connection connection, DatosResponseWesterUnionDTO datos) throws Exception{
    	String 	spName 				= "pr_ActualizaTransaccion_V1";
    	String 	spEjecutar 			= null;
    	CallableStatement cs 		= null;
    	
    	try{
    		spEjecutar 				= "{call " + spName + "( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? "
    				                                    + ", ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ?)}";
    		cs 						= connection.prepareCall(spEjecutar);
    		cs.setQueryTimeout(5);
    		
			//@idRegistroAct 
			if(datos.getIdRegistroActualiza() != null && !datos.getIdRegistroActualiza().isEmpty()){        
				cs.setString(1, datos.getIdRegistroActualiza());
			}else{
				cs.setString(1, "");
			}
			//@secuencialBR
			if(datos.getSecuenciaBR() !=null && !datos.getSecuenciaBR().isEmpty()){        
				cs.setString(2, datos.getSecuenciaBR());
			}else{
				cs.setString(2, "");
			}
			//@secuencialSARA
			if(datos.getSecuenciaSara() != null && !datos.getSecuenciaSara().isEmpty()){        
				cs.setString(3, datos.getSecuenciaSara());
			}else{
				cs.setString(3, "");
			}
			//@autorizacionSARA
			if(datos.getAutorizacionSara() != null && !datos.getAutorizacionSara().isEmpty()){        
				cs.setString(4, datos.getAutorizacionSara());
			}else{
				cs.setString(4, "");
			}
			//@fechaSARA 
			if(datos.getFechaSARA() != null && !datos.getFechaSARA().isEmpty()){        
				cs.setString(5, datos.getFechaSARA());
			}else{
				cs.setString(5, "");
			}
			//@tipoServicio	
			if(datos.getTipoPago() != null && !datos.getTipoPago().isEmpty()){        
				cs.setString(6, datos.getTipoPago());
			}else{
				cs.setString(6, "");
			}
			//@descripcionTipSer
			if(datos.getDescripcionPago() != null && !datos.getDescripcionPago().isEmpty()){        
				cs.setString(7, datos.getDescripcionPago());
			}else{
				cs.setString(7, "");
			}
			//@nombreCliente
			if(datos.getNombreCliente() != null && !datos.getNombreCliente().isEmpty()){        
				cs.setString(8, datos.getNombreCliente());
			}else{
				cs.setString(8, "");
			}
			//@documentoCliente
			if(datos.getDocumentoCliente() != null && !datos.getDocumentoCliente().isEmpty()){        
				cs.setString(9, datos.getDocumentoCliente());
			}else{
				cs.setString(9, "");
			}
			//@observacionWU
			if(datos.getObservacionPago() != null && !datos.getObservacionPago().isEmpty()){        
				cs.setString(10, datos.getObservacionPago());
			}else{
				cs.setString(10, "");
			}
			//@direccionCliente
			if(datos.getDireccionObser() != null && !datos.getDireccionObser().isEmpty()){        
				cs.setString(11, datos.getDireccionObser());
			}else{
				cs.setString(11, "");
			}
			//@valorPagarCli
			if(datos.getValorPagar() != null && !datos.getValorPagar().isEmpty()){        
				cs.setString(12, datos.getValorPagar());
			}else{
				cs.setString(12, "");
			}
			//@valorAbonadoCli
			if(datos.getValorAbonadoCliente() != null && !datos.getValorAbonadoCliente().isEmpty()){        
				cs.setString(13, datos.getValorAbonadoCliente());
			}else{
				cs.setString(13, "");
			}
			//@valorTotalPagarCli
			if(datos.getValorTotalPagar() != null && !datos.getValorTotalPagar().isEmpty()){        
				cs.setString(14, datos.getValorTotalPagar());
			}else{
				cs.setString(14, "");
			}
			//@valorMinimoCli 
			if(datos.getValorminimoPagar() != null && !datos.getValorminimoPagar().isEmpty()){        
				cs.setString(15, datos.getValorminimoPagar());
			}else{
				cs.setString(15, "");
			}
			//@valorRecargo 
			if(datos.getValorRecargo() != null && !datos.getValorRecargo().isEmpty()){        
				cs.setString(16, datos.getValorRecargo());
			}else{
				cs.setString(16, "");
			}
			//@fechaVencimineto
			if(datos.getFechaVencimientoPago() != null && !datos.getFechaVencimientoPago().isEmpty()){        
				cs.setString(17, datos.getFechaVencimientoPago());
			}else{
				cs.setString(17, "");
			}
			//@numFactura 
			if(datos.getNumFactura() != null && !datos.getNumFactura().isEmpty()){        
				cs.setString(18, datos.getNumFactura());
			}else{
				cs.setString(18, "");
			}
			//@codigoRespuestaWU
			if(datos.getCodigoRespuestaWU() != null && !datos.getCodigoRespuestaWU().isEmpty()){        
				cs.setString(19, datos.getCodigoRespuestaWU());
			}else{
				cs.setString(19, "");
			}
			//@mensajeRespuestaWU
			if(datos.getMensajeRespuestaWU() != null && !datos.getMensajeRespuestaWU().isEmpty()){        
				cs.setString(20, datos.getMensajeRespuestaWU());
			}else{
				cs.setString(20, "");
			}
			//@tipoDocCliente
			if(datos.getTipoDocumento() != null && !datos.getTipoDocumento().isEmpty()){        
				cs.setString(21, datos.getTipoDocumento());
			}else{
				cs.setString(21, "");
			}
			//@valorSugeridoWU
			if(datos.getValorPagoSugerido() != null && !datos.getValorPagoSugerido().isEmpty()){        
				cs.setString(22, datos.getValorPagoSugerido());
			}else{
				cs.setString(22, "");
			}
			//@tipoCliente
			if(datos.getTipoClienteFac() != null && !datos.getTipoClienteFac().isEmpty()){        
				cs.setString(23, datos.getTipoClienteFac());
			}else{
				cs.setString(23, "");
			}
			//@telefonoCliente
			if(datos.getTelefonoClienteFac() != null && !datos.getTelefonoClienteFac().isEmpty()){        
				cs.setString(24, datos.getTelefonoClienteFac());
			}else{
				cs.setString(24, "");
			}
			//@emailCliente
			if(datos.getEmailClienteFac() != null && !datos.getEmailClienteFac().isEmpty()){        
				cs.setString(25, datos.getEmailClienteFac());
			}else{
				cs.setString(25, "");
			}
			//@referenciaConsulta
			if(datos.getReferenciaConsultaBR() != null && !datos.getReferenciaConsultaBR().isEmpty()){        
				cs.setString(26, datos.getReferenciaConsultaBR());
			}else{
				cs.setString(26, "");
			}
			//@codigoRespuestaBR
			if(datos.getCodigoRespuestaBR() != null && !datos.getCodigoRespuestaBR().isEmpty()){        
				cs.setString(27, datos.getCodigoRespuestaBR());
			}else{
				cs.setString(27, "");
			}
			
			//@printTicket
			if(datos.getTicketImpresion() != null && !datos.getTicketImpresion().isEmpty()){        
				cs.setString(28, datos.getTicketImpresion());
			}else{
				cs.setString(28, "");
			}
			
			//@ISOTicket
			if(datos.getTicketISOimpresion() != null && !datos.getTicketISOimpresion().isEmpty()){        
				cs.setString(29, datos.getTicketISOimpresion());
			}else{
				cs.setString(29, "");
			}
			
			//@autorizacionBR
			if(datos.getAutorizacionBR() != null && !datos.getAutorizacionBR().isEmpty()){        
				cs.setString(30, datos.getAutorizacionBR());
			}else{
				cs.setString(30, "");
			}
			
			cs.execute();
			cs.close();  
    	}catch(Exception e){
    		log.error(this, e);
    		e.printStackTrace();
    	}
    }    

}
