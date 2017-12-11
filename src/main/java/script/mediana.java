package script;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class mediana extends JRDefaultScriptlet{
    ArrayList<Double> aCh = new ArrayList<Double>(); 
    ArrayList<Double> aType = new ArrayList<Double>();
    ArrayList<Double> aRep = new ArrayList<Double>();
	
	public mediana() {
		// TODO Auto-generated constructor stub
	}
	
	public Double getTypeMediana(){
		return gatAm(aType);
	}

	public Double getChMediana(){
		return gatAm(aCh);
	}
	public Double getRepMediana(){
		return gatAm(aRep);
	}
	
	private Double gatAm(ArrayList<Double> A){
//		System.out.println("items.size() = "+A.size());
		if (A.size() == 0) return 0.0;
//		System.out.println("items.size() int = "+((int)((items.size()+1) / 2) - 1));
		Collections.sort(A);
		if (A.size() == 1) return A.get(0);
//		System.out.println("items.size() int = "+(((int)((A.size()+1) / 2)) - 1));
		Double ret = A.get( ((int)((A.size()+1) / 2)) - 1);
//		if (!items.isEmpty()) items.clear();
		return ret;
	}
	
	public void beforeGroupInit(String groupName){
	 if (groupName.equals("type") && !aType.isEmpty()) aType.clear();
	 if (groupName.equals("Chnl") && !aCh.isEmpty()) aCh.clear();
//	 System.out.println("new group = " + groupName);
	}
	
	public void afterGroupInit(String groupName){
		
	}
		
	public void beforeDetailEval(){
		
	}
		
	public void afterDetailEval() throws JRScriptletException{
	try{
	   aCh.add(((BigDecimal)getFieldValue("AM")).doubleValue());
	   aType.add(((BigDecimal)getFieldValue("AM")).doubleValue());
	   aRep.add(((BigDecimal)getFieldValue("AM")).doubleValue());
	}catch(Exception e){
		System.out.println("afterDetailEval = " + e.getMessage());
	}
	}
}
