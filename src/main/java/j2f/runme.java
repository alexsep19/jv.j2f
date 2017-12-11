package j2f;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.*;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
//import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.JasperFillManager;
//import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

public class runme {
	String fFileName; 
	String fJaspName;
	HashMap<String, Object> fJaspPar;
	Connection fRepConn;
	String fFormat;
	static String JASPER_PATH = null, JASPER_SWAP_PATH = null;
	static int swapPages = 0;
	JRFileVirtualizer virtualizer;
//	String dateFrom = null, dateTo = null;
	JasperPrint fJasperPrint = null;

	static {
		JASPER_PATH = System.getProperty("rsys.jasper", "/WORK/exe/jaspers/");
		JASPER_SWAP_PATH = System.getProperty("rsys.swap.path", "/tmp/jasp");
		String s = System.getProperty("rsys.swap.pages", "10");
		try{
		    swapPages = Integer.parseInt(s);
		    System.out.println("jJasp2File: rsys.swap.pages = "+swapPages);
		}catch(Exception e){
		    System.out.println("jJasp2File: number need: rsys.swap.pages = "+s);
//		    swapPages = 10;
		}
	}
	
	public runme(String pFileName, String pJaspName, HashMap pJaspPar, Connection pRepConn, String pFormat) {
		fFileName = pFileName + "." + pFormat; 
		fJaspName = pJaspName;
		fJaspPar = pJaspPar;
		fRepConn = pRepConn;
		fFormat = pFormat;
	}
	
	public runme(JasperPrint pJasperPrint, String pFileName, String pJaspName, HashMap pJaspPar, Connection pRepConn, String pFormat) {
		fFileName = pFileName + "." + pFormat; 
		fJaspName = pJaspName;
		fJaspPar = pJaspPar;
		fRepConn = pRepConn;
		fFormat = pFormat;
		fJasperPrint = pJasperPrint;
	}
	
	
    public String MakeFile() {
    	String ret = "OK";
    	JasperReport jasperReport;
    	java.util.Map<java.lang.String,JRPrintAnchorIndex> anch = new java.util.HashMap<java.lang.String,JRPrintAnchorIndex>();
    	HashMap<String, Object> m = new HashMap<String, Object>();
//    	boolean isSvodFee = false;
    	JasperPrint jasperPrint = null;
    	String XmlSource = null, XmlOut = null;
    	Document doc = null;
//    	System.out.println("MakeFile");
        try{
//        jasperReport = JasperCompileManager.compileReport( "C://sample_report.jrxml");
        jasperReport = (JasperReport)JRLoader.loadObject(new File(JASPER_PATH + fJaspName + ".jasper"));
        for(String key: fJaspPar.keySet()){
          if (key.substring(0,2).equals("P_")){
/*        	if (key.equals("P_SVOD_FEE")){
        	  isSvodFee = fJaspPar.get(key).equals("Y");
        	} else if (key.equals("P_DATE_FROM")){
        	  dateFrom = fJaspPar.get(key).toString();
        	} else if (key.equals("P_DATE_TO")){
        	  dateTo = fJaspPar.get(key).toString();
        	}
         	}else{
*/         	
              for (int i = 0; i < jasperReport.getParameters().length; i++){
//        	System.out.println("for key = "+key+"; jasperReport"+jasperReport.getParameters()[i].getName());
            	if (jasperReport.getParameters()[i].getName().equals(key)){
                  if (jasperReport.getParameters()[i].getValueClassName().equals("java.lang.Double")){
                	m.put(key, Double.valueOf(fJaspPar.get(key).toString() ));
                  }else m.put(key, fJaspPar.get(key).toString());
//            	  System.out.println("key = "+key+";fJaspPar.get(key) = "+fJaspPar.get(key).toString());
            	  break;
            	 }
              }
            }
          }
//          JRSwapFileVirtualizer virtualizer = null;
//          virtualizer = new JRSwapFileVirtualizer(swapPages, new JRSwapFile(JASPER_SWAP_PATH, 4096, 4096), true); 
//          JRFileVirtualizer virtualizer = null;
        virtualizer = new JRFileVirtualizer(swapPages, JASPER_SWAP_PATH); 
        m.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
  	   XmlSource = jasperReport.getProperty("PR_XML_SOURCE");
//       System.out.println("XmlSource = " + XmlSource);

//        m.put("P_XML_SOURCE", "QQQQQQQQQ" + XmlSource);
        if (fJasperPrint == null) {
        	if (XmlSource != null){
        	  Xml2Rep o = new Xml2Rep();	
      		  Method[] methods = Xml2Rep.class.getDeclaredMethods();
    		   for (int i = 0; i < methods.length; i++) {
    			  if (methods[i].getName().equals(XmlSource)){
    				doc = (Document)methods[i].invoke( o, new Object[] { m, fRepConn});
//    				String isXmlFile = jasperReport.getProperty("PR_XML_FILE");
//   			        System.out.println("PR_XML_FILE = " + jasperReport.getProperty("PR_XML_FILE"));
    				if (jasperReport.getProperty("PR_XML_FILE").equals("Y")){
    				  fFormat = "FILE";     					
    				}else{
//    				 JRXmlDataSource jrx = (JRXmlDataSource)methods[i].invoke( o, new Object[] { m, fRepConn, jasperReport.getQuery().getText()});
//    				 m.put("P_XML_SOURCE", XmlOut);
    				 JRXmlDataSource jrx = new JRXmlDataSource(doc, jasperReport.getQuery().getText());
    				 jasperPrint = JasperFillManager.fillReport(jasperReport, m, jrx);
//    				jasperPrint = JasperFillManager.fillReport(jasperReport, m, fRepConn);
    				}
    				break;
    			   }
    		     }

        	}else  jasperPrint = JasperFillManager.fillReport(jasperReport, m, fRepConn);
        }else jasperPrint = fJasperPrint;
        
        virtualizer.setReadOnly(true);
        
        if (fFormat.equals("XLS")){
//          JExcelApiExporter exporterXLS = new JExcelApiExporter(); 
          JRXlsxExporter exporterXLS = new JRXlsxExporter();
          exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,Boolean.TRUE);
          exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,Boolean.TRUE);
          exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,Boolean.FALSE);
          exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
//          exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
          exporterXLS.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
          exporterXLS.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fFileName);
          exporterXLS.exportReport();
        }else if (fFormat.equals("CSV")){
        	JRCsvExporter exporterCSV = new JRCsvExporter();
            exporterCSV.setParameter(JRCsvExporterParameter.JASPER_PRINT, jasperPrint);
            exporterCSV.setParameter(JRCsvExporterParameter.OUTPUT_FILE_NAME, fFileName);
            exporterCSV.setParameter(JRCsvExporterParameter.CHARACTER_ENCODING, "UTF-8");//Cp1251
            exporterCSV.setParameter(JRCsvExporterParameter.RECORD_DELIMITER, "\r\n");
            exporterCSV.exportReport();
        }else if (fFormat.equals("RTF")){
            JRRtfExporter exporterRTF = new JRRtfExporter();
            exporterRTF.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporterRTF.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fFileName);
            exporterRTF.exportReport();
        }else if (fFormat.equals("XML")){
            JRXmlExporter exporterXML = new JRXmlExporter();
            exporterXML.setParameter(JRXmlExporterParameter.JASPER_PRINT, jasperPrint);
            exporterXML.setParameter(JRXmlExporterParameter.OUTPUT_FILE_NAME, fFileName);
            exporterXML.setParameter(JRXmlExporterParameter.IS_EMBEDDING_IMAGES, Boolean.TRUE);
            exporterXML.exportReport();
        }else if (fFormat.equals("FILE")){
   		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
    	  Transformer transformer = transformerFactory.newTransformer();
    	  DOMSource source = new DOMSource(doc);
  		  StreamResult result = new StreamResult(new File(fFileName.substring(0,fFileName.lastIndexOf(".")) + ".XML"));
//          transformer.setOutputProperty(OutputKeys., true);
//  	  transformer.setOutputProperty(arg0, arg1)
    	  transformer.transform(source, result);
        }else throw new Exception("Формат "+fFormat+" не обрабатывается");
        ret = fFileName;
        }catch(Exception e){
        	ret = "Jasp MakeFile Err = " + e.getMessage();
        	e.printStackTrace();
        }finally{
            virtualizer.cleanup();
        }
        
          return(ret);
         
     }
    
//    TCC39697;0343;M;0.0003;56927.71;17;EUR
/*    final int FLD_FEE_NAME = 0, FLD_FIN = 1, FLD_PS = 2, FLD_FEE = 3, FLD_VOL = 4, FLD_AM = 5, FLD_CUR = 6 ;
    private void PutBase(String rec) throws Exception{
    	String[] flds = rec.split(";");
    	ResultSet result = null;
    	Statement st = null;
    	String isExists = "Select count(*) val from reporter.ba_svod_fee ";
    	String Where =  " where QUART = to_date('"+dateFrom+"','dd-mm-yyyy') " +
    	                   	" and FIN_CODE = '" + flds[FLD_FIN] + "'" +
    	                   	" and PS = '" + flds[FLD_PS] + "'" +
    	                    " and FEE_NAME = '" + flds[FLD_FEE_NAME]+"'";;
    	String Ins = "Insert into reporter.ba_svod_fee (QUART,FIN_CODE,PS,FEE_NAME,FEE,VOL,AM,CURR) values("+
    	             "to_date('"+dateFrom+"','dd-mm-yyyy')," +
    	             "'" + flds[FLD_FIN] + "','"+ flds[FLD_PS] + "','"+ flds[FLD_FEE_NAME] + "',"+ 
    	             flds[FLD_FEE] + ","+ flds[FLD_VOL] + ","+ flds[FLD_AM] + ",'" + flds[FLD_CUR]+"')";
    	String Upd = "Update reporter.ba_svod_fee set FEE = " + flds[FLD_FEE] + "," +
    	                                             "VOL = " + flds[FLD_VOL] + "," +
    	                                             "AM = " + flds[FLD_AM] + "," +
    	                                             "CURR = '"+ flds[FLD_CUR]+"'"; 
   	
    	
    try{	
    	st = fRepConn.createStatement();
    	result = st.executeQuery(isExists + Where);
    	result.next();
    	int num = result.getInt(1);
    	st = fRepConn.createStatement();
    	if (num == 0){
   		  st.executeUpdate(Ins);
    	}else{
          st.executeUpdate(Upd + Where);
    	}
    	
    }finally{
    	if (result != null) result.close();
    	if (st != null) st.close();
    }
    	
    }
    */
//    public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//    	HashMap<String, String> m = new HashMap<String, String>();
//    	m.put("P_S_DATE_FROM", "sss");
//    	m.put("P_S_DATE_TO", "DDD");
//    	m.put("P_N_USD", "123,3");
//    	m.put("P_N_EUR", "34.3");
//    	m.put("P_FOR_DATE", "01-04-2012");
//        OraConn conn = new OraConn();
//        Connection cnProd = conn.GetOraConn("PROD_CONN_REP", "PROD_USER_REP", "PROD_PASS_REP");

//    	runme o = new runme("d:/WORK/Bins", "Bins", m, cnProd, "XML");
//        runme o = new runme("d:/WORK/f410_3", "f410_3", m, cnProd, "XLS");
//		String r = o.MakeFile();
//		System.out.println(" = " + r);
    	
//		HashMap<String, String> m = new HashMap<String, String>();
//    	m.put("P_DATE_FROM", "01-10-2011");
//    	m.put("P_DATE_TO", "31-12-2011");
//    	m.put("P_USD", "30");
//    	m.put("P_EUR", "40");
//    	m.put("P_SVOD_FEE", "Y");
//        OraConn conn = new OraConn();
//        Connection cnProd = conn.GetOraConn("PROD_CONN_REP", "PROD_USER_REP", "PROD_PASS_REP");
//    	JasperPrint jasperPrint = JRPrintXmlLoader.load("d:/WORK/McMIssObo2.xml");
//
//        j2f.runme j = new j2f.runme( jasperPrint, "d:/WORK/McMIssObo2", "McMIssObo2", m, cnProd, "XXX");

        
//    	runme o = new runme("d:/WORK/McMIssObo2", "McMIssObo2", m, cnProd, "XML");
//    	runme o = new runme("d:/WORK/Bins", "Bins", m, cnProd, "XML");
//		String r = j.MakeFile();
//		System.out.print(" = " + r);

//	}
}
