package j2f;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class ScripTechno extends JRDefaultScriptlet{

    public String getSampleString(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9) 
	    throws JRScriptletException{ 
	return p1 + "|" + p2 + "|" + p3 + "|" + p4 + "|" + p5 + "|" + p6 + "|" + p7 + "|" + p8 + "|" + p9; 
    }    
    
    public static void main(String[] args) {
	// TODO Auto-generated method stub

    }

}
