package anytools;

import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonLog {
	public static final String LEVEL_M = "M";
	public static final String LEVEL_E = "E";
	public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
	public static final long DEF_USER = 1;
	public static final int RET_SUCCESS = 0;
	public static final int RET_NOFORMAT = 1;
	public static final int RET_NOVERSION = 2;
	public static final int RET_NORUN = 3;
	public static final int RET_ARRAYFILLED = 4;
	public static final int RET_TIMEOUT = 5;
	public static final int RET_NOSERVLET = 6;
	public static final int RET_ERROR = 7;
	public static final int RET_ERLEVEL = 8;
	public static final int RET_WITHOUTRESP = 9;

    private static final double LOG_VERSION = 0.1;
    private long user;
    private String levelLog;
    private String messType;
    private String message;
    private String project;
    private String classMet;
    private String trace;
    private java.util.Date date;

	public JsonLog() {}
	public JsonLog(String Mess, String ClassMet, String LevelLog, String MessType, String Project, String Trace, long UserId) {
		message = Mess;
		classMet = ClassMet;
		levelLog = LevelLog;
		messType = MessType;
		project = Project;
		trace = Trace;
		user = UserId;
	}
	
	public JSONObject obj2json(){
		JSONObject retObj = new JSONObject();
		JSONObject objData = new JSONObject();
		retObj.put("version", LOG_VERSION);
		
		objData.put("user", user == 0 ? DEF_USER: user);
		objData.put("levelLog", levelLog == null? "": levelLog);
		objData.put("messType", messType == null? "": messType);
		objData.put("message", message == null? "": message);
		objData.put("project", project == null? "": project);
		objData.put("classMet", classMet == null? "": classMet);
		objData.put("trace", trace == null? "": trace);
		objData.put("date", new SimpleDateFormat(DATE_FORMAT).format(new java.util.Date()));
		retObj.put("data", objData);

		return retObj;
	}
	
    public int json2obj(InputStreamReader Json){
    	int ret = RET_NOFORMAT;
try{
    	JSONObject o = (JSONObject)JSONValue.parse(Json);
//    	System.out.println("o.get(\"version\") = "+o);
    	if ((Double) o.get("version") < LOG_VERSION ) return RET_NOVERSION;
  	    
    	JSONObject data = (JSONObject)o.get("data"); 
    	user = (Long)data.get("user");
    	levelLog = data.get("levelLog").toString();
    	messType = data.get("messType").toString();
    	message = data.get("message").toString();
    	project = data.get("project").toString();
    	classMet = data.get("classMet").toString();
    	trace = data.get("trace").toString();
    	date = new SimpleDateFormat(DATE_FORMAT).parse(data.get("date").toString());
    	ret = RET_SUCCESS;
}catch(Exception e){
	    System.out.println("json2obj error " + e.getMessage());	
	    e.printStackTrace();
}       	
     return ret;
	}
    
	public static String stackTraceToString(Exception e) {
	    StringBuilder sb = new StringBuilder();
	    for (StackTraceElement element : e.getStackTrace()) {
	        sb.append(element.toString()).append("\n");
	    }
//	    System.out.println("-------------------------");
//	    System.out.println(sb.toString());
//	    System.out.println("-------------------------");
	    return sb.toString();
	}

	public void WriteLog(String url){
		JsonSend js = new JsonSend(this, url);
		js.Send(false);
	}
	
    public long getUser() {
		return user;
	}

	public void setUser(long user) {
		this.user = user;
	}

	public String getLevelLog() {
		return levelLog;
	}

	public void setLevelLog(String levelLog) {
		this.levelLog = levelLog;
	}

	public String getMessType() {
		return messType;
	}

	public void setMessType(String messType) {
		this.messType = messType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getClassMet() {
		return classMet;
	}

	public void setClassMet(String classMet) {
		this.classMet = classMet;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	public String getStringDate() {
		return date == null? "": new SimpleDateFormat(DATE_FORMAT).format(date);
	}

	public java.util.Date getDate() {
		return date;
	}


}
