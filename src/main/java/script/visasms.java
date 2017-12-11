package script;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class visasms extends JRDefaultScriptlet{
    public String getRNN(String mess) throws JRScriptletException{
	String ret = "XXX";
//	Pattern p = Pattern.compile("(RRN +(\\d)+)");
//	Pattern pp = Pattern.compile("((\\d)+)");
	Matcher m = Pattern.compile("(RRN +(\\d)+)").matcher(mess);
	if (m.find()) {
	    Matcher mm = Pattern.compile("((\\d)+)").matcher(m.group(1));
	    if (mm.find()) ret = mm.group(1);
	}
	return ret; 
    }    
    
    public BigDecimal getSum(String mess) throws JRScriptletException{
	BigDecimal ret = new BigDecimal(0);
	Matcher m = Pattern.compile("(sum += +(\\d)+.(\\d)+)").matcher(mess);
	if (m.find()) {
	    Matcher mm = Pattern.compile("((\\d)+.(\\d)+)").matcher(m.group(1));
try{	    
	    if (mm.find()) ret = new BigDecimal(mm.group(1));
}catch(Exception e){
    ret = new BigDecimal(-1);
}
	}
	return ret;
    }
    
    public String getFile(String mess) throws JRScriptletException{
	String ret = "XXX";
	Matcher m = Pattern.compile("(\\[file.+\\])").matcher(mess);
	if (m.find()) {
	    Matcher mm = Pattern.compile("(SMS[A-Z0-9.]+)").matcher(m.group(1));
	    if (mm.find()) ret = mm.group(1);
	}
	return ret;
    }
    
}
