/**
    This file is part of "CTSI MCW Deidentification" for removing
    protected health information from medical records.

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    "CTSI MCW Deidentification" is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with "CTSI MCW Deidentification."  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @author jayurbain, jay.urbain@gmail.com
 * 
 */
package edu.mcw.ctsi.deid;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import deidentification.DeIdentificationThread;
import deidentification.DeidentificationRegex;
import deidentification.MedicalRecordWrapper;
import deidentification.NamedEntityRecognition;

import util.SimpleIRUtilities;

import org.apache.sling.commons.json.JSONObject;

/*
 * Servlet that performs patient de-identification
 */
public class DeidServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
//	private static final Logger LOGGER = Logger.getLogger(DeidServiceServlet.class);
	private static final NumberFormat formatter = new DecimalFormat("#0.00000");
	
	private static NamedEntityRecognition namedEntityRecognition = null;
	private static DeidentificationRegex deidentificationRegex = null;
	
	private static Map<String, String> whiteListMap = new HashMap<String, String>();
	private static Map<String, String> blackListMap = new HashMap<String, String>();
	
	public static NamedEntityRecognition getNamedEntityRecognition() {
		return namedEntityRecognition;
	}

	public static void setNamedEntityRecognition(NamedEntityRecognition namedEntityRecognition) {
		DeidServiceServlet.namedEntityRecognition = namedEntityRecognition;
	}

	public static DeidentificationRegex getDeidentificationRegex() {
		return deidentificationRegex;
	}

	public static void setDeidentificationRegex(DeidentificationRegex deidentificationRegex) {
		DeidServiceServlet.deidentificationRegex = deidentificationRegex;
	}

	public static Map<String, String> getWhiteListMap() {
		return whiteListMap;
	}

	public static void setWhiteListMap(Map<String, String> whiteListMap) {
		DeidServiceServlet.whiteListMap = whiteListMap;
	}

	public static Map<String, String> getBlackListMap() {
		return blackListMap;
	}

	public static void setBlackListMap(Map<String, String> blackListMap) {
		DeidServiceServlet.blackListMap = blackListMap;
	}

	public static NumberFormat getNumberFormatter() {
		return formatter;
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

        String whitelistfilename = getServletContext().getRealPath( config.getInitParameter("whitelistfilename") );
        String blacklistfilename = getServletContext().getRealPath( config.getInitParameter("blacklistfilename") );
        String namedentityrecognitionclass = config.getInitParameter("namedentityrecognitionclass");
        String regexdeidentificationclass = config.getInitParameter("regexdeidentificationclass");

        String[] whiteListArray = null;
        String[] blackListArray = null;

        //////////////////////////////////////////////////
        // read white list - pass through list
        File whitelistfile = new File(whitelistfilename);
        File backlistfile = new File(blacklistfilename);
        try {
            whiteListArray = loadFileList(whitelistfile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // read black list - block list
        try {
            blackListArray = loadFileList(backlistfile);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        whiteListMap = new HashMap<String, String>();
        blackListMap = new HashMap<String, String>();

        if (whiteListArray != null && whiteListArray.length > 0) {
            for (int i = 0; i < whiteListArray.length; i++) {
                String[] stringArray = whiteListArray[i].split("\\s+");
                for (int j = 0; j < stringArray.length; j++) {
                    String s = stringArray[j].toLowerCase();
                    whiteListMap.put(s, s);
                }
            }
        }
        if (blackListArray != null && blackListArray.length > 0) {
            for (int i = 0; i < blackListArray.length; i++) {
                String[] stringArray = blackListArray[i].split("\\s+");
                for (int j = 0; j < stringArray.length; j++) {
                    String s = stringArray[j].toLowerCase();
                    blackListMap.put(s, s);
                }
            }
        }
        
        try {
            namedEntityRecognition = (NamedEntityRecognition) Class.forName(namedentityrecognitionclass).newInstance();
            System.out.println("CLASS TO DEID WITH : " + namedentityrecognitionclass);
            deidentificationRegex = (DeidentificationRegex) Class.forName(regexdeidentificationclass).newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            e1.printStackTrace();
//            System.exit(-1);
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        namedEntityRecognition.setWhiteListMap(whiteListMap);
        namedEntityRecognition.setBlackListMap(blackListMap);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long start = System.currentTimeMillis();
		PrintWriter out = response.getWriter();
		String text = request.getParameter("q");
		String patientName = request.getParameter("name");
		if( patientName == null || patientName.length() == 0) {
			patientName = null;
		}
		String dateoffsetStr = request.getParameter("dateoffset");
        int dateOffset = 0;
        
        try {
			if( dateoffsetStr != null) {
				dateOffset = Integer.parseInt( dateoffsetStr );
			}
		} catch (NumberFormatException e1) {
			dateOffset = 0;
		}
		String id = request.getParameter("id");
		String note_id = request.getParameter("noteid");

		String format = request.getParameter("format");
		MedicalRecordWrapper record = null;
		if (text != null && text.trim().length() > 0) {
			try {
//	                String id = results.getString(1);
//	                String note_id = results.getString(2);
//	                //Supply an empty string instead of null if the Oracle value is "NULL"
//	                String text = results.getString(3) != null ? results.getString(3) : "";

                java.util.Date startDateRegex = new java.util.Date();
                String preprocessedText = deidentificationRegex.compositeRegex(text, dateOffset, blackListMap, patientName);
                java.util.Date endDateRegex = new java.util.Date();
                record = new MedicalRecordWrapper(id, note_id, text, preprocessedText, null, dateOffset, patientName);
                long msecs = SimpleIRUtilities.getElapsedTimeMilliseconds(startDateRegex, endDateRegex);
                record.setMillisecondsRegex(msecs);
                record.setDeIdText( namedEntityRecognition.performAnnotation( record.getRegexText() ) );
				String elapsed = getFormatter()
						.format((System.currentTimeMillis() - start) / 1000d);
				System.out.println(record);
				out.println(record.getDeIdText() + "<p/><i> Processed in " + elapsed + " secs</i>");
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public static NumberFormat getFormatter() {
		return formatter;
	}
	
    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    static String[] loadFileList(File file) throws IOException {

        List<String> list = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        while ((str = in.readLine()) != null) {
            String s = str.trim();
            if (s.length() > 0) {
                s = str.toLowerCase(); // normalize to upper case
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

}
