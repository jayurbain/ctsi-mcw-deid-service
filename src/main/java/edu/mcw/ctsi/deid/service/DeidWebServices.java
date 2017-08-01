package edu.mcw.ctsi.deid.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import deidentification.MedicalRecordWrapper;
import edu.mcw.ctsi.deid.DeidServiceServlet;
import util.SimpleIRUtilities;

/**
 * Servlet implementation class NlpWebServices
 */
public class DeidWebServices extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeidWebServices() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String q = request.getParameter("q");
		processServiceRequest(q, request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doGet(request, response);
		
		StringBuffer jb = new StringBuffer();
		  String line = null;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }

//		  try {
//		    JSONObject jsonObject =  HTTP.toJSONObject(jb.toString());
//		  } catch (JSONException e) {
//		    // crash and burn
//		    throw new IOException("Error parsing JSON request string");
//		  }
		  
		  processServiceRequest(jb.toString(), request, response);

		  // Work with the data using methods like...
		  // int someInt = jsonObject.getInt("intParamName");
		  // String someString = jsonObject.getString("stringParamName");
		  // JSONObject nestedObj = jsonObject.getJSONObject("nestedObjName");
		  // JSONArray arr = jsonObject.getJSONArray("arrayParamName");
		  // etc...
	}
	
	protected void processServiceRequest(String q, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println(q);
		
	    response.setContentType("application/json");
	    PrintWriter out=response.getWriter();
		
		List<MedicalRecordWrapper> medicalRecordList = null;
		try {
			medicalRecordList = jsonToJava(q);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		long start = System.currentTimeMillis();
		
		StringBuffer sbb = new StringBuffer();
		try {
			boolean first = true;
			sbb.append("{ \"deidList\": [");
			for( MedicalRecordWrapper record : medicalRecordList) {	

				try {
	                java.util.Date startDateRegex = new java.util.Date();
	                String preprocessedText = DeidServiceServlet.getDeidentificationRegex().compositeRegex(
	                		record.getText(), record.getDateOffset(), DeidServiceServlet.getBlackListMap(), record.getPatientName());
	                record.setRegexText(preprocessedText);
	                java.util.Date endDateRegex = new java.util.Date();
	                long msecs = SimpleIRUtilities.getElapsedTimeMilliseconds(startDateRegex, endDateRegex);
	                record.setMillisecondsRegex(msecs);
	                record.setDeIdText( DeidServiceServlet.getNamedEntityRecognition().performAnnotation( record.getRegexText() ) );

					System.out.println(record);
//					out.println(record.getDeIdText() + "<p/><i> Processed in " + elapsed + " secs</i>");
				} catch (Exception e) {
					throw new ServletException(e);
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String name = "";
		int dateoffset = 0;
		if( medicalRecordList.size() > 0 ) {
			name = medicalRecordList.get(0).getPatientName();
			dateoffset = medicalRecordList.get(0).getDateOffset();
		}
		
		String result = "{\"name\":\""+name+ "\",\"dateoffset\":\""+dateoffset+"\",\"deidlist\":" +
				createJSONDeidArray(medicalRecordList) + "}";
		
		String elapsed = DeidServiceServlet.getFormatter()
				.format((System.currentTimeMillis() - start) / 1000d);
		
		System.out.println(result);
		System.out.println(elapsed);
	    out.write(result);
	    out.close();
	}
	
	public static List<MedicalRecordWrapper> jsonToJava(String json) throws JSONException {
		
		List<MedicalRecordWrapper> recordList = new ArrayList<MedicalRecordWrapper>();
		int dateOffset = 0;
		JSONObject jsonObject = new JSONObject( json );
		String name;
		try {
			name = (String) jsonObject.getString("name");
		} catch (Exception e) {
			name = null;
		}
		String dateoffsetStr;
		try {
			dateoffsetStr = (String) jsonObject.getString("dateoffset");
		} catch (Exception e) {
			dateoffsetStr = null;
		}
        try {
			if( dateoffsetStr != null) {
				dateOffset = Integer.parseInt( dateoffsetStr );
			}
		} catch (NumberFormatException e1) {
			dateOffset = 0;
		}
		JSONArray records = (JSONArray) jsonObject.get("recordlist");
		for(int i=0; i<records.length(); i++) {
			String text = records.getString(i);
			recordList.add( new MedicalRecordWrapper(""+i, ""+i, text, null, null, dateOffset, name) );
		}
		return recordList;
	}
	
	public static String createJSONDeidArray( List<MedicalRecordWrapper> list ) {
		
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		sb.append("[");
		for( MedicalRecordWrapper medicalRecordWrapper :  list ) {
			if( first ) {
				first = false;
			}
			else {
				sb.append(",");
			}
			sb.append( "\"" + medicalRecordWrapper.getDeIdText() + "\"");
		}
		sb.append("]");
		return sb.toString();
	}
}