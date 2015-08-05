package com.tester.mockall.mockserver;

import org.mockserver.model.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by vivek.agr on 4/15/15.
 */
public class ExpectationElements {

    public static final String reqPathKey = "ReqwithPath";
    public static final String reqQueryKey = "ReqwithQueryStringParameter";
    public static final String reqBodyKey  = "ReqwithBody";
    public static final String reqCookieKey = "ReqwithCookies";
    public static final String reqHeaderKey  = "ReqwithHeaders";
    public static final String reqMethodKey = "ReqwithMethod";

    public static final String resBodyKey  = "ResBody";
    public static final String resCookieKey = "ReswithCookies";
    public static final String resHeaderKey  = "ReswithHeaders";
    public static final String resDelayKey = "ReswithDelay";
    public static final String resStatusKey = "ReswithStatusCode";



    public String getReqwithPath(HashMap map) {
        if (map.containsKey(reqPathKey)) {
            return (String) map.get(reqPathKey);
        } else {
            return null;
        }
    }

    public  List<Parameter> getReqwithQueryStringParameter(HashMap map) {
        List<Parameter> queryList = new ArrayList<Parameter>();
        if (map.containsKey(reqQueryKey)) {

            String paramlist = (String) map.get(reqQueryKey);

            List<String> valList = new ArrayList<String>();
            String[] paramList = paramlist.split("&");
            for (int i = 0; i < paramList.length; i++) {

                String[] temp = paramList[i].split("=");

                //Iterate the List to find multiple values for the name
                if(temp.length == 1) {
                    valList.add(temp[0]);
                } else {
                    String[] tempParam = temp[1].split(";");
                    for (int j = 0; j < tempParam.length; j++) {
                        valList.add(tempParam[j]);
                    }
                }

                queryList.add(new Parameter(temp[0],valList));
                valList.clear();
            }
            return queryList;
        } else {
            return null;
        }
    }

    /* This returns the body with regex=
     */
    public Body getReqwithBody(HashMap map) {

        if (map.containsKey(reqBodyKey)) {
            String reqBody = (String) map.get(reqBodyKey).toString();
            if (reqBody.endsWith("json")) {
                reqBody = readFileAsString(reqBody);
            }

            if (reqBody.startsWith("regex=")) {
                return new StringBody(reqBody.trim(), Body.Type.REGEX);
            } else {
                return new StringBody(reqBody.trim(), Body.Type.JSON);
            }
        } else {
            return null;
        }
    }

    public List<Cookie> getReqwithCookies(HashMap map) {
        List<Cookie> cookieList = new ArrayList<Cookie>();
        if (map.containsKey(reqCookieKey)) {

            String paramlist = (String) map.get(reqCookieKey);

            String[] paramList = paramlist.split("&");
            for (int i = 0; i < paramList.length; i++) {
                String[] temp = paramList[i].split("=",2);

                /*
                This is to handle regular expressions and error scenarios
                 */
                if(temp.length == 1) {
                    cookieList.add(new Cookie(temp[0], temp[0]));
                } else {
                    cookieList.add(new Cookie(temp[0], temp[1]));
                }
            }
            return cookieList;
        } else {
            return null;
        }
    }

    public List<Header> getReqwithHeaders(HashMap map) {
        List<Header> headerList = new ArrayList<Header>();
        if (map.containsKey(reqHeaderKey)) {
            String paramlist = (String) map.get(reqHeaderKey);
            String[] paramList = paramlist.split("&");

            for (int i = 0; i < paramList.length; i++) {
                String[] temp = paramList[i].split(":");

                /*
                This is to handle regular expressions and error scenarios
                 */
                if(temp.length == 1) {
                    headerList.add(new Header(temp[0], temp[0]));
                } else {
                    headerList.add(new Header(temp[0], temp[1]));
                }
            }
            return headerList;
        } else {
            return null;
        }
    }


/* getters for the response expectation starts here*/

    public String getReqwithMethod(HashMap map) {
        if(map.containsKey(reqMethodKey)) {
            return (String) map.get(reqMethodKey);
        } else  {
            return null;
        }
    }

    public List<Header> getReswithHeaders(HashMap map) {
        if (map.containsKey(resHeaderKey)) {
            String paramlist = (String) map.get(resHeaderKey);

            List<Header> headerList = new ArrayList<Header>();

            String[] paramList = paramlist.split("&");
            for (int i = 0; i < paramList.length; i++) {
                String[] temp = paramList[i].split(":");

                if(temp.length == 1) {
                    headerList.add(new Header(temp[0], temp[0]));
                } else {
                    headerList.add(new Header(temp[0], temp[1]));
                }
            }
            return headerList;
        } else {
            return null;
        }
    }

    public List<Header> getReswithHeaders(HashMap map, String candidate) {
        if (map.containsKey(candidate)) {
            String paramlist = (String) map.get(candidate);

            List<Header> headerList = new ArrayList<Header>();

            String[] paramList = paramlist.split("&");
            for (int i = 0; i < paramList.length; i++) {
                String[] temp = paramList[i].split(":");

                if(temp.length == 1) {
                    headerList.add(new Header(temp[0], temp[0]));
                } else {
                    headerList.add(new Header(temp[0], temp[1]));
                }

                headerList.add(new Header(temp[0], temp[1]));
            }

            return headerList;
        } else {
            return null;
        }
    }

    public String getReswithBody(HashMap map) {
        if (map.containsKey(resBodyKey)) {

            String resBody = map.get(resBodyKey).toString();

            if (resBody.endsWith("json")) {
                resBody = readFileAsString(resBody);
            }
            return resBody;
        } else {
            return null;
        }
    }


    public String getReswithBody(HashMap map, String candidate) {
        if (map.containsKey(candidate)) {
            String resBody = (String) map.get(candidate).toString();

            if (resBody.endsWith("json")) {
                resBody = readFileAsString(resBody);
            }
            return resBody;
        } else {
            return null;
        }
    }

    public Delay getResDelay(HashMap map) {
        if (map.containsKey(resDelayKey)) {
            return (new Delay(TimeUnit.MILLISECONDS, Integer.parseInt((String)map.get(resDelayKey))));
        } else {
            //int randomDelay = (int) ((Math.random()) * 5000) + 2000 ;
            //return (new Delay(TimeUnit.MILLISECONDS, randomDelay));
            return null;
        }


    }

    public int getReswithStatusCode(HashMap map) {

        if (map.containsKey(resStatusKey)) {
            return Integer.parseInt( map.get(resStatusKey).toString());
        } else {
            return 200;
        }
    }


    public int getReswithStatusCode(HashMap map, String candidate) {

        if (map.containsKey(candidate)) {
            return Integer.parseInt((String) map.get(resStatusKey));
        } else {
            return 200;
        }
    }


    public List<Cookie> getReswithCookies(HashMap map) {
        List<Cookie> cookieList = new ArrayList<Cookie>();

        if (map.containsKey(resCookieKey)) {
            String paramlist = (String) map.get(resCookieKey);

            String[] paramList = paramlist.split("&");
            for (int i = 0; i < paramList.length; i++) {
                String[] temp = paramList[i].split("=",2);

                if(temp.length == 1) {
                    cookieList.add(new Cookie(temp[0], temp[0]));
                } else {
                    cookieList.add(new Cookie(temp[0], temp[1]));
                }
            }
            return cookieList;
        } else {
            return null;
        }
    }

    public List<Cookie> getReswithCookies(HashMap map, String candidate) {
        List<Cookie> cookieList = new ArrayList<Cookie>();
        if (map.containsKey(candidate)) {
            String paramlist = (String) map.get(candidate);

            String[] paramList = paramlist.split("&");
            for (int i = 0; i < paramList.length; i++) {
                String[] temp = paramList[i].split("=",2);

                if(temp.length == 1) {
                    cookieList.add(new Cookie(temp[0], temp[0]));
                } else {
                    cookieList.add(new Cookie(temp[0], temp[1]));
                }
            }

            return cookieList;
        } else {
            return null;
        }
    }


    public static String readFileAsString(String filePath) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line, results = "";
            while ((line = reader.readLine()) != null) {
                results += line;
            }
            reader.close();
            return results;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
