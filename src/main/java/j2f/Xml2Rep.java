package j2f;

import java.io.File;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
//import org.apache.xpath.CachedXPathAPI

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sf.jasperreports.engine.JRPrintAnchorIndex;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
//import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

public class Xml2Rep {

public Document getMsAct(HashMap Params, Connection fRepConn){
	 String fMOnTurnP = null, fMOnTurnN = null, fMOnInP = null, fMOnInN = null, 
			fMMoTurnP = null, fMMoTurnN = null, fMMoInP = null, fMMoInN = null,
	        fMNfClN = null, fMNfClP	= null, fMNfCdN = null, fMNfCdP	= null, fMNfAcN = null, fMNfAcP	= null,
	        fMDisconnect = null, fInDisconnect = null,
	        fSmsServN = null, fSmsServP = null, fSmsServCardN = null, fSmsServCardP = null,
	        fSmsServAccN = null, fSmsServAccP = null,
	        fSmsRboMobDD = null, fSmsRboDD = null,
	        fSmsClTurnN = null, fSmsClTurnP = null, fSmsCdTurnN = null, fSmsCdTurnP = null, fSmsMsDD = null;
		String fMOnPayP = null, fMMoPayP = null, fEmailClTurnN = null,	fEmailClTurnP = null,
		fEmailCdTurnN = null, fEmailCdTurnP = null;

 	 Document doc = null;
 	String DateFrom = null, DateTo = null;
	 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	 Connection conn = null;
     ResultSet rs = null; //fMNfClN, fMNfClP
	 PreparedStatement pstmt = null;
	 CallableStatement St = null;
//	 String sql_rbo = "";
try{
	for(Object key: Params.keySet()){
//      System.out.println("r = " + r + "; key = " + key);
	 if (((String)key).equals("P_FDM")){
		 DateFrom = Params.get(key).toString();
	 }
	 if (((String)key).equals("P_LDM")){
		 DateTo = Params.get(key).toString();
	 }
    }

	//================ Way4
	String sql_way4 = "with X as (select TRUNC(FI.REC_DATE - 15, 'MM') M"+
	 " ,AC.CONTRACT_NUMBER"+
	 " ,DECODE(SUBSTR(F.BRANCH_CODE, 2, 1), '1', 'MDMINFO', 'SMSINFO') TERR"+
	 " ,DECODE(D.AMND_STATE, 'C', 'SP', '') SP"+
	 " ,ROW_NUMBER() OVER(partition by TRUNC(FI.REC_DATE - 15, 'MM'), AC.ID order by D.ID) RN,AC.ID"+
	 " from FILE_INFO FI, FILE_RECORD FR, DOC D, ACNT_CONTRACT AC, F_I F"+
	 " where FI.FILE_TYPE = 'TRN'"+
	 " and FI.FILE_CHAIN_STATUS = 'P'"+
	 " and FI.CREATION_DATE between ADD_MONTHS(to_date(?1,'DD-MM-YYYY'), -2) - 1 and"+
	 " ADD_MONTHS(to_date(?2,'DD-MM-YYYY'), 1) + 1"+
	 " and FR.FILE_INFO__OID = FI.ID"+
	 " and FR.RECORD_TYPE = 'D'"+
	 " and D.ID = FR.REF_RECORD"+
	 " and D.SOURCE_CODE = 'PAYSMSMON'"+
	 " and NVL(D.MESSAGE_CATEGORY, 'U') = 'U'"+
	 " and AC.AMND_STATE = 'A'"+
	 " and AC.CONTRACT_NUMBER = TRANSLATE(D.TARGET_NUMBER, ' R', ' ')"+
	 " and F.ID = AC.F_I"+
	 " and (F.BRANCH_CODE like '01__' or F.BRANCH_CODE like '00__')),"+
	 " LASTM as"+
	 " (select /*+ materialize */*"+
	 " from X"+
	 " where M = ADD_MONTHS(to_date(?3,'DD-MM-YYYY'), -1)),"+
	 " NEXTM as"+
	 " (select /*+ materialize */ *"+
	 " from X"+
	 " where M = to_date(?4,'DD-MM-YYYY')),"+
	 " ANALYTICS as"+
	 " (select /*+ materialize */"+
	 " LASTM.M LASTM"+
	 " ,NEXTM.M NEXTM"+
	 " ,NVL(SY_ADD_DATA.GET_LABEL_RO('ACNT_CONTRACT', 'UNC', AC.ID), AC.CLIENT__ID) UNC"+
	 " ,DECODE(LASTM.M, null,"+
	 " NVL(SY_ADD_DATA.GET_LABEL_RO('ACNT_CONTRACT', 'UNC', AC.ID), AC.CLIENT__ID), null) UNCINV"+
	 " ,NVL(NEXTM.SP, NEXTM.TERR) type"+
	 " from LASTM"+
	 " right outer join NEXTM"+
	 " on 1 = 1"+
	 " and LASTM.ID = NEXTM.ID"+
	 " and LASTM.RN = NEXTM.RN"+
  " inner join ACNT_CONTRACT AC"+
     " on AC.ID = NVL(LASTM.ID, NEXTM.ID))"+
" select count(NEXTM) - count(LASTM) MNTH"+
      " ,count(NEXTM) TOTAL"+
      " ,count(distinct UNC) CL"+
      " ,count(distinct UNCINV) CLM"+
      " ,type"+
  " from ANALYTICS group by type";
		try{
		    pstmt = fRepConn.prepareStatement( sql_way4 );
		    pstmt.setString(1, DateFrom);
		    pstmt.setString(2, DateTo);
		    pstmt.setString(3, DateFrom);
		    pstmt.setString(4, DateFrom);
		    rs = pstmt.executeQuery();
		    while(rs.next()){
			String t = rs.getString("type");
			if (t.equals("MDMINFO")){
			  fMNfCdN = rs.getString("TOTAL");
			  fMNfCdP = rs.getString("MNTH");
			}else if (t.equals("SP")){
			  fSmsServCardN = rs.getString("TOTAL");
			  fSmsServCardP = rs.getString("MNTH");
			}else if (t.equals("SMSINFO")){
			  fSmsClTurnN = rs.getString("CL"); 
			  fSmsClTurnP = rs.getString("CLM");
			  fSmsCdTurnN = rs.getString("TOTAL");
			  fSmsCdTurnP = rs.getString("MNTH");
			}
		    }
			
		}catch(Exception e){
		       System.out.println("getMsAct way4 = " + e.getMessage()); 
			   e.printStackTrace();
		 }
	//================ RBO
	try{	
		conn = ((DataSource)InitialContext.doLookup(System.getProperty("rsys.jdbc.rbo"))).getConnection();
//		St = conn.prepareCall("{call ibs.z$BANK_CLIENT_MDM_LIB.sms_active(to_date(?,'dd-mm-yyyy'), to_date(?,'dd-mm-yyyy'), ?,?,?,?,?)}");
		St = conn.prepareCall("{call ibs.z$BANK_CLIENT_MDM_LIB.sms_active(to_date(?,'dd-mm-yyyy'), to_date(?,'dd-mm-yyyy'), ?,?,?,?,?,?, ?,?,?,?,?,?)}");
		St.setString(1, DateFrom);
		St.setString(2, DateTo);
		St.setString(3, "1");
		St.setString(4, "1");
		St.setString(5, "1");
		St.setString(6, "1");
		St.setString(7, "1");
		St.setString(8, "1");
		St.setString(9, "0");//0
		St.setString(10, "0");
		St.setString(11, "1");
		St.setString(12, "1");
		St.setString(13, "1");//0
		St.setString(14, "1");
	        St.registerOutParameter(3, java.sql.Types.VARCHAR);
		St.registerOutParameter(4, java.sql.Types.VARCHAR);
		St.registerOutParameter(5, java.sql.Types.VARCHAR);
		St.registerOutParameter(6, java.sql.Types.VARCHAR);
		St.registerOutParameter(7, java.sql.Types.VARCHAR);
		St.registerOutParameter(8, java.sql.Types.VARCHAR);

//		St.registerOutParameter(9, java.sql.Types.VARCHAR);
//		St.registerOutParameter(10, java.sql.Types.VARCHAR);
		St.registerOutParameter(11, java.sql.Types.VARCHAR);
		St.registerOutParameter(12, java.sql.Types.VARCHAR);
		St.registerOutParameter(13, java.sql.Types.VARCHAR);
		St.registerOutParameter(14, java.sql.Types.VARCHAR);

	    St.execute();
//		fSmsClTurnN = St.getString(3);
//		fSmsClTurnP = St.getString(4);		 
//		fSmsCdTurnN = St.getString(5);
//		fSmsCdTurnP = St.getString(6);
//		fSmsMsDD = St.getString(7);
		fMNfClN = St.getString(3); 
		fMNfClP = St.getString(4);
		fMNfAcN = St.getString(5);
        fMNfAcP = St.getString(6);
        fSmsServN = St.getString(7);
        fSmsServP = St.getString(8);
//        fSmsServCardN = St.getString(9);//
//        fSmsServCardP = St.getString(10);
        fSmsServAccN = St.getString(11);
        fSmsServAccP = St.getString(12);
        fSmsRboMobDD = St.getString(13);//
        fSmsRboDD = St.getString(14);
//        fSmsRboMobDD � fSmsServCardN
		
	 }catch(Exception e){
	       System.out.println("getMsAct conn RBO = " + e.getMessage()); 
		   e.printStackTrace();
		 }finally{
		  try{if (rs != null) rs.close(); rs = null;}catch(Exception ee1){};
		  try{if (pstmt != null) pstmt.close(); pstmt = null;}catch(Exception ee){};
		  try{if (conn != null) conn.close(); conn = null;}catch(Exception eee){}; 
		 }
	//================ TELE	
//String sql_tele = "Select fMOnTurnN, fMOnTurnP, fMOnInN, fMOnInP, fMMoTurnN, fMMoTurnP, "+
//                "fMMoInN, fMMoInP, fSmsMsDD "+
//                ", fMOnPayP, fMMoPayP, fEmailClTurnN, fEmailClTurnP, fEmailCdTurnN, fEmailCdTurnP"+
//        " from dbo.nik_MegaStat(convert(datetime, ?, 105), convert(datetime, ?, 105))";
//	String sql_tele = "{call dbo.nik_MegaStat(convert(datetime, ?, 105), convert(datetime, ?, 105))}";
//	String sql_tele = "{call dbo.nik_MegaStat(?, ?)}";
String sql_tele = "Select fMOnTurnN, fMOnTurnP, fMOnInN, fMOnInP, fMMoTurnN, fMMoTurnP, "+
  "fMMoInN, fMMoInP, fSmsMsDD "+
  ", fMOnPayP, fMMoPayP, fEmailClTurnN, fEmailClTurnP, fEmailCdTurnN, fEmailCdTurnP, fMDisconnect, fInDisconnect"+
  " from dbo.nik_MegaStat(?, ?)";
		try{	
			conn = ((DataSource)InitialContext.doLookup(System.getProperty("rsys.jdbc.tele"))).getConnection();
			CallableStatement cs = conn.prepareCall(sql_tele);
			cs.setString(1, DateFrom);
		    cs.setString(2, DateTo);
		    boolean results = cs.execute();
		    int rowsAffected = 0;
		    
	        // Protects against lack of SET NOCOUNT in stored prodedure
	        while (results || rowsAffected != -1) {
	            if (results) {
	                rs = cs.getResultSet();
	                break;
	            } else {
	                rowsAffected = cs.getUpdateCount();
	            }
	            results = cs.getMoreResults();
	        }
//			pstmt = conn.prepareStatement( sql_tele );
//			pstmt.setString(1, DateFrom);
//		    pstmt.setString(2, DateTo);
//			rs = pstmt.executeQuery();
			rs.next();
			fMOnTurnN = rs.getString("fMOnTurnN"); fMOnTurnP = rs.getString("fMOnTurnP"); 
			fMOnInN = rs.getString("fMOnInN");  fMOnInP = rs.getString("fMOnInP"); 
			fMMoTurnN = rs.getString("fMMoTurnN"); fMMoTurnP = rs.getString("fMMoTurnP");
			fMMoInN = rs.getString("fMMoInN"); fMMoInP = rs.getString("fMMoInP"); 
			fMDisconnect = rs.getString("fMDisconnect"); fInDisconnect = rs.getString("fInDisconnect");
//			fMNfCdN = rs.getString("fMNfCdN"); fMNfCdP = rs.getString("fMNfCdP");
//			fSmsClTurnN = rs.getString("fSmsClTurnN"); fSmsClTurnP = rs.getString("fSmsClTurnP");
//			fSmsCdTurnN = rs.getString("fSmsCdTurnN"); fSmsCdTurnP = rs.getString("fSmsCdTurnP");
			fSmsMsDD = rs.getString("fSmsMsDD");
			
			fMOnPayP = rs.getString("fMOnPayP");
			fMMoPayP = rs.getString("fMMoPayP");
			fEmailClTurnN = rs.getString("fEmailClTurnN");
			fEmailClTurnP = rs.getString("fEmailClTurnP");
			fEmailCdTurnN = rs.getString("fEmailCdTurnN");
			fEmailCdTurnP = rs.getString("fEmailCdTurnP");
		 }catch(Exception e){
	       System.out.println("getMsAct conn tele = " + e.getMessage()); 
		   e.printStackTrace();
		 }finally{
		  try{if (rs != null) rs.close(); rs = null;}catch(Exception ee1){};
		  try{if (pstmt != null) pstmt.close(); pstmt = null;}catch(Exception ee){};
		  try{if (conn != null) conn.close(); conn = null;}catch(Exception eee){}; 
		 }
	        
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	doc = docBuilder.newDocument();
	Element rootElement = doc.createElement("REP");
	doc.appendChild(rootElement);
	
	addField( doc, rootElement, fMOnTurnN, "fMOnTurnN");
	addField( doc, rootElement, fMOnInN, "fMOnInN");
	addField( doc, rootElement, fMOnTurnP, "fMOnTurnP");
	addField( doc, rootElement, fMOnInP, "fMOnInP");
	addField( doc, rootElement, fMMoTurnN, "fMMoTurnN");
	addField( doc, rootElement, fMMoTurnP, "fMMoTurnP");
	addField( doc, rootElement, fMMoInN, "fMMoInN");
	addField( doc, rootElement, fMMoInP, "fMMoInP");
	addField( doc, rootElement, fMNfClN, "fMNfClN");
	addField( doc, rootElement, fMNfClP, "fMNfClP");
	addField( doc, rootElement, fMNfCdN, "fMNfCdN");
	addField( doc, rootElement, fMNfCdP, "fMNfCdP");
	addField( doc, rootElement, fMNfAcN, "fMNfAcN");
	addField( doc, rootElement, fMNfAcP, "fMNfAcP");

	addField( doc, rootElement, fMDisconnect, "fMDisconnect");
	addField( doc, rootElement, fInDisconnect, "fInDisconnect");
	
	addField( doc, rootElement, fSmsServN, "fSmsServN");
	addField( doc, rootElement, fSmsServP, "fSmsServP");
	addField( doc, rootElement, fSmsServCardN, "fSmsServCardN");
	addField( doc, rootElement, fSmsServCardP, "fSmsServCardP");
	addField( doc, rootElement, fSmsServAccN, "fSmsServAccN");
	addField( doc, rootElement, fSmsServAccP, "fSmsServAccP");
	addField( doc, rootElement, fSmsRboMobDD, "fSmsRboMobDD");
	addField( doc, rootElement, fSmsRboDD, "fSmsRboDD");
	
	addField( doc, rootElement, fSmsClTurnN, "fSmsClTurnN");
	addField( doc, rootElement, fSmsClTurnP, "fSmsClTurnP");
	addField( doc, rootElement, fSmsCdTurnN, "fSmsCdTurnN");
	addField( doc, rootElement, fSmsCdTurnP, "fSmsCdTurnP");
	addField( doc, rootElement, fSmsMsDD, "fSmsMsDD");
	
	addField( doc, rootElement, fMOnPayP, "fMOnPayP");
	addField( doc, rootElement, fMMoPayP, "fMMoPayP");
	addField( doc, rootElement, fEmailClTurnN, "fEmailClTurnN");
	addField( doc, rootElement, fEmailClTurnP, "fEmailClTurnP");
	addField( doc, rootElement, fEmailCdTurnN, "fEmailCdTurnN");
	addField( doc, rootElement, fEmailCdTurnP, "fEmailCdTurnP");
	
//	Element MOnTurnN = doc.createElement("fMOnTurnN");
//	MOnTurnN.appendChild(doc.createTextNode(fMOnTurnN==null?"null":fMOnTurnN));
//	rootElement.appendChild(MOnTurnN);
	}catch(Exception e){
		  e.printStackTrace();
		  System.out.print(e.getMessage());
	  }
	 return doc;
	}

	private void addField(Document d, Element r, String v, String name){
		Element e = d.createElement(name);
		e.appendChild(d.createTextNode( v == null? name: v));
		r.appendChild(e);
	}
	
public Document getMsSms(HashMap Params, Connection fRepConn){
    	Document doc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		String DateFrom = null, DateTo = null, fldTeleDir = null;
		String fldWay4CardMos = null, fldWay4Acc = null, fldWay4CardNoMos = null,
			   fldRbo = null;
		Connection conn = null;
        ResultSet rs = null;
	    PreparedStatement pstmt = null;
	    String sql_tele = "select count(distinct clientid) from sms "+
                          "where (upper(message) like  '%BAL%' or upper(message) like '%STM%') "+ 
                          "and data >= convert(datetime, ?, 105) "+
                          "and data < convert(datetime, ?, 105)+1";
	    String sql_way4 = "select count(decode(a.doc_id, null, 1, null)) acc "+
                          ",count(decode(f.bank_code, '0001', 1, null)) card_mos "+
                          ",count(decode(f.bank_code, '0001', null, null, null, 1)) card_nomos "+
                          "from proc.sms#archive a "+
                          ",ows.EVNT_MSG em, usage_action ua,ows.acnt_contract ac, ows.f_i f "+
                          "where A.RESP_CLASS in ('OK','INFO') "+ 
                          "and send_time >= to_date(?1, 'dd-mm-yyyy') "+
                          "and send_time < to_date(?2, 'dd-mm-yyyy') + 1 "+
                          "and em.id (+)= a.doc_id "+
                          "and ua.id (+)= em.usage_action__oid "+
                          "and ac.id (+)= ua.acnt_contract__id "+
                          "and ac.amnd_state (+)= 'A' "+ 
                          "and f.id (+)= ac.f_i "+
                          "and f.amnd_state (+)= 'A'";
	    String sql_rbo = "select count(1) from IBS.Z#BC_SMS_ARC S1 "+
                         "where S1.C_CREATED between TO_DATE(?1, 'DD/MM/YYYY') and "+
                         "TO_DATE(?2, 'DD/MM/YYYY') "+
                         "and S1.C_STATUS = 'SENT' "+
                         "and S1.C_IS_IN = '0' ";

try{
	for(Object key: Params.keySet()){
//      System.out.println("r = " + r + "; key = " + key);
	 if (((String)key).equals("P_DATE_FROM")){
		 DateFrom = Params.get(key).toString();
	 }
	 if (((String)key).equals("P_DATE_TO")){
		 DateTo = Params.get(key).toString();
	 }
    }
//=============== ���  
	try{
		conn = ((DataSource)InitialContext.doLookup(System.getProperty("rsys.jdbc.rbo"))).getConnection();
		pstmt = conn.prepareStatement( sql_rbo );
		pstmt.setString(1, DateFrom);
	    pstmt.setString(2, DateTo);
		rs = pstmt.executeQuery();
		rs.next();
		fldRbo = rs.getString(1);
	 }catch(Exception e){
	       System.out.println("getMsSms conn rbo = " + e.getMessage()); 
		   e.printStackTrace();
	 }finally{
	  try{if (rs != null) rs.close(); rs = null;}catch(Exception ee1){};
	  try{if (pstmt != null) pstmt.close(); pstmt = null;}catch(Exception ee){};
	  try{if (conn != null) conn.close(); conn = null;}catch(Exception eee){}; 
	 }
//================ ����	
	try{	
		conn = ((DataSource)InitialContext.doLookup(System.getProperty("rsys.jdbc.tele"))).getConnection();
		pstmt = conn.prepareStatement( sql_tele );
		pstmt.setString(1, DateFrom);
	    pstmt.setString(2, DateTo);
		rs = pstmt.executeQuery();
		rs.next();
		fldTeleDir = rs.getString(1);
	 }catch(Exception e){
       System.out.println("getMsSms conn tele = " + e.getMessage()); 
	   e.printStackTrace();
	 }finally{
	  try{if (rs != null) rs.close(); rs = null;}catch(Exception ee1){};
	  try{if (pstmt != null) pstmt.close(); pstmt = null;}catch(Exception ee){};
	  try{if (conn != null) conn.close(); conn = null;}catch(Exception eee){}; 
	 }
//================ �������	
	try	{
		pstmt = fRepConn.prepareStatement( sql_way4 );
		pstmt.setString(1, DateFrom);
	    pstmt.setString(2, DateTo);
		rs = pstmt.executeQuery();
		rs.next();
		fldWay4Acc = rs.getString(1);
		fldWay4CardMos = rs.getString(2);
		fldWay4CardNoMos = rs.getString(3);
		
	}catch(Exception e){
	       System.out.println("getMsSms way4 = " + e.getMessage()); 
		   e.printStackTrace();
	 }
	
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	doc = docBuilder.newDocument();
	Element rootElement = doc.createElement("REP");
	doc.appendChild(rootElement);
	
	Element Way4CardNoMos = doc.createElement("fldWay4CardNoMos");
	Way4CardNoMos.appendChild(doc.createTextNode(fldWay4CardNoMos==null?"null":fldWay4CardNoMos));
	rootElement.appendChild(Way4CardNoMos);
	
	Element Way4Acc = doc.createElement("fldWay4Acc");
	Way4Acc.appendChild(doc.createTextNode(fldWay4Acc==null?"null":fldWay4Acc));
	rootElement.appendChild(Way4Acc);
	
	Element TeleDir = doc.createElement("fldTeleDir");
	TeleDir.appendChild(doc.createTextNode(fldTeleDir==null?"null":fldTeleDir));
	rootElement.appendChild(TeleDir);

	Element Rbo = doc.createElement("fldRbo");
	Rbo.appendChild(doc.createTextNode(fldRbo==null?"null":fldRbo));
	rootElement.appendChild(Rbo);
	
	Element Way4CardMos = doc.createElement("fldWay4CardMos");
	Way4CardMos.appendChild(doc.createTextNode(fldWay4CardMos==null?"null":fldWay4CardMos));
	rootElement.appendChild(Way4CardMos);

}catch(Exception e){
	  e.printStackTrace();
	  System.out.print(e.getMessage());
  }
        return doc;
	}
	
    String[] Reps = {"McMIssObo2_2", "McAcqObo2_2", "McIssCard2", "VisaIssOboMonth_2", "VisaIssCard_2", "VisaAcqObo_2"};
    public Document getAgSvodFee(HashMap Params, Connection fRepConn){
//        public JRXmlDataSource getAgSvodFee(HashMap Params, Connection fRepConn, String xPath){
//        JRXmlDataSource jrx = null;
    	Document doc = null;
        JasperPrint jasperPrint = null;
        JasperReport jasperReport = null;
        java.util.Map<java.lang.String,JRPrintAnchorIndex> anch = new java.util.HashMap<java.lang.String,JRPrintAnchorIndex>();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

//        McAcqObo2_2 McMIssObo2_2 VisaAcqObo_2 VisaIssCard_2 VisaIssOboMonth_2
     try{
    	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("FEES");
		doc.appendChild(rootElement);
    	Element its = doc.createElement("ITS");
		rootElement.appendChild(its);
		for(String r: Reps){
      	   jasperReport = (JasperReport)JRLoader.loadObject(new File(runme.JASPER_PATH + r + ".jasper"));
           jasperPrint = JasperFillManager.fillReport(jasperReport, Params, fRepConn);
           anch = jasperPrint.getAnchorIndexes();
           for(String key: anch.keySet()){
//             System.out.println("r = " + r + "; key = " + key);
             if (key != null) addIt(doc, its, key.split(";"));
           }
		  }
		
//		String key1 = "0317 ����;M;Domestic, International + Cash On-Us, Domestic, International);10567.49;0.0003;3.17;EUR";
//		addIt(doc, its, key1.split(";"));
//		jrx = new JRXmlDataSource(doc, xPath);
/*	     TransformerFactory transformerFactory = TransformerFactory.newInstance();
		 Transformer transformer = transformerFactory.newTransformer();
		 DOMSource source = new DOMSource(doc);
		 StringWriter writer = new StringWriter();
		 transformer.setOutputProperty(OutputKeys.ENCODING, "WINDOWS-1251");
		 transformer.transform(source, new javax.xml.transform.stream.StreamResult(writer));
		 OutXml = writer.toString();
		 writer.close();
		 */
         }catch(Exception e){
      	  e.printStackTrace();
      	  System.out.print(e.getMessage());
        }
        return doc;
      }
    
    private void addIt(Document Doc, Element Its, String[] s) throws Exception{
    	if (s.length != 8) throw new Exception("Len of Anchor = " + s.length);
//    	if (Fin != null) Fin = s[0];
    	
    	Element it = Doc.createElement("IT");
		Its.appendChild(it);
		Element ItFi = Doc.createElement("IT_FI");
		ItFi.appendChild(Doc.createTextNode(s[0]));
		it.appendChild(ItFi);
    	
		Element ItPc = Doc.createElement("IT_PC");
		ItPc.appendChild(Doc.createTextNode(s[1]));
		it.appendChild(ItPc);
    	
		Element ItType = Doc.createElement("IT_TYPE");
		ItType.appendChild(Doc.createTextNode(s[2]));
		it.appendChild(ItType);

		Element ItName = Doc.createElement("IT_NAME");
		ItName.appendChild(Doc.createTextNode(s[3]));
		it.appendChild(ItName);
		
		Element ItObo = Doc.createElement("IT_OBO");
		ItObo.appendChild(Doc.createTextNode(s[4]));
		it.appendChild(ItObo);
		
		Element ItPer = Doc.createElement("IT_PER");
		ItPer.appendChild(Doc.createTextNode(s[5]));
		it.appendChild(ItPer);
		
		Element ItCom = Doc.createElement("IT_COM");
		ItCom.appendChild(Doc.createTextNode(s[6]));
		it.appendChild(ItCom);
		
		Element ItCur = Doc.createElement("IT_CUR");
		ItCur.appendChild(Doc.createTextNode(s[7]));
		it.appendChild(ItCur);
    }

}
