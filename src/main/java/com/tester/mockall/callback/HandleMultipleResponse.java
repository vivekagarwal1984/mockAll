package com.tester.mockall.callback;

import com.csvreader.CsvReader;
import com.tester.mockall.instantiate.MainClass;
import com.tester.mockall.mockserver.ExpectationElements;
import org.apache.log4j.Logger;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tester.mockall.mockserver.Expectation.reqToMulResponse;

/**
 * Created by vivek.agr on 1/6/15.
 */

public class HandleMultipleResponse implements ExpectationCallback,Cloneable {

    private static final Object locker = new Object();
    static Logger logger = Logger.getLogger(
            HandleMultipleResponse.class);
    public HandleMultipleResponse() {

    }

    public static synchronized void displayQueue() {
        logger.debug("Printing the Queue");

        Iterator<HashMap> ite = reqToMulResponse.keySet().iterator();
        while (ite.hasNext()) {
            HashMap temptMap = ite.next();
            logger.debug("Request HashMap:" + temptMap.toString());

            Deque<HttpResponse> resQ = reqToMulResponse.get(temptMap);
            for (Iterator itr = resQ.iterator(); itr.hasNext(); ) {
                logger.debug(itr.next());
            }

        }
        logger.debug("Done with printing the queue");
    }

    @Override
    public synchronized HttpResponse handle(HttpRequest var1) {

        //displayQueue();
        logger.debug("------------------------------------------------------------------------------------------------");
        logger.info("Searching response for request = "+var1.toString());
        HttpResponse resToSend = new HttpResponse();


        //Find out if the incoming request is already in the Queue.
        //Function to handle regular expressions
        synchronized (locker) {
            Iterator<HashMap> ith = reqToMulResponse.keySet().iterator();
            while(ith.hasNext()) {
                HashMap tempMap = ith.next();
                if (RequestMatcher.checkRequestMatch(var1, tempMap) == 0) {
                    logger.info("Request found in the Queue");
                    resToSend = findResponseFromMap(tempMap);
                    return resToSend;
                }

            }
        }

        logger.info("Request not found in the Queue!");
        try {
            synchronized (locker) {
                //find the filename
                // Extract the port number from the request header's host
                String[] reqHost = var1.getFirstHeader("Host").split(":");
                String reqPort = reqHost[1];
                System.out.println("reqPort = " + reqPort);

                BufferedReader br = new BufferedReader(new FileReader(MainClass.configFile));
                String line;
                while (((line = br.readLine()) != null) && (!reqToMulResponse.containsKey(var1))) {
                    int flag =0;
                    logger.debug("Creating the queue for the first time");
                    String[] param = line.split(":");
                    String filename = param[0];
                    String portNum = param[1];

                    if (reqPort.equals(portNum)) {

                        logger.debug("filename:" + filename);
                        reqToMulResponse = createMap(var1, filename);

                        Iterator<HashMap> ite = reqToMulResponse.keySet().iterator();
                        while (ite.hasNext()) {
                            HashMap temptMap = ite.next();
                            if (RequestMatcher.checkRequestMatch(var1, temptMap) == 0) {
                                resToSend = findResponseFromMap(temptMap);
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 1) break;
                        logger.debug("Not found in the filename:" + filename);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resToSend;
    }

    public HashMap<HashMap, Deque<HttpResponse>> createMap(HttpRequest request, String filename) throws IOException {

        HashMap expectationMap = new HashMap();

        HashMap<HashMap, Deque<HttpResponse>> tmap = new HashMap<HashMap, Deque<HttpResponse>>();

        if(filename.endsWith("csv")) {

            CsvReader expectations = new CsvReader(filename, '#');
            HashMap defaultexpectationmap = new HashMap();
            expectations.readHeaders();

            int expectationCount = 0;

            while(expectations.readRecord()) {
                if (expectationCount != 0) {
                    expectationMap = (HashMap) defaultexpectationmap.clone();
                }

                if (((expectations.get("testid")).equalsIgnoreCase("default")) && (defaultexpectationmap.isEmpty())) {
                    for (int i = 0; i < expectations.getHeaderCount(); i++) {
                        defaultexpectationmap.put(expectations.getHeader(i), expectations.get(expectations.getHeader(i)));
                    }

                } else {
                    for (int i = 0; i < expectations.getHeaderCount(); i++) {
                        if (expectations.get(expectations.getHeader(i)) != "") {
                            expectationMap.put(expectations.getHeader(i), expectations.get(expectations.getHeader(i)));
                        }
                    }
                    logger.debug(expectationMap.toString());

                    Deque<HttpResponse> resQ = readRespone(expectationMap);
                    tmap.put(expectationMap, resQ);
                }
                expectationCount++;
            }
        }

        else if (filename.endsWith("yml")) {

            Yaml yaml = new Yaml();
            InputStream input = new FileInputStream(new File(filename));
            Object data = yaml.load(input); // load the yaml document into a java object
            HashMap<String,HashMap> map = (HashMap<String,HashMap>) data;

            for(Map.Entry<String, HashMap> e: map.entrySet()) {
                HashMap expectationmap = new HashMap();
                Iterator it = e.getValue().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    expectationmap.put(pair.getKey(), pair.getValue());
                    it.remove();
                }

                logger.debug(expectationmap.toString());

                Deque<HttpResponse> resQ = readRespone(expectationmap);
                tmap.put(expectationmap, resQ);
            }
        }
        return tmap;
    }

    public HttpResponse findResponseFromMap(HashMap temp) {

        HttpResponse response;
        displayQueue();
        response = reqToMulResponse.get(temp).removeFirst();
        reqToMulResponse.get(temp).addLast(response);
        response.applyDelay();
        logger.info("Response returned : " + response.toString());
        logger.info("------------------------------------------------------------------------------------------------");
        return response;
    }

    public Deque<HttpResponse> readRespone (HashMap expectationMap) {

        Deque resQ = new ArrayDeque();

        HttpResponse fResponse = readDefaultResponse(expectationMap);


        resQ.add(fResponse);
        int count = 2;

        int exist = 0;
        while(exist == 0) {
            exist = 1;
            String regex = ".*"+count;

            Pattern p = Pattern.compile(regex);

            HttpResponse nextRes = readDefaultResponse(expectationMap);

            Iterator<String> ite = expectationMap.keySet().iterator();
            while(ite.hasNext()) {

                String candidate = ite.next();
                ExpectationElements expect = new ExpectationElements();

                Matcher m = p.matcher(candidate);
                if(m.matches()) {
                    logger.debug("Matching candidate:" + candidate);
                    //Add the values to the response
                    if (candidate.compareToIgnoreCase("Body") > 0) {
                        nextRes.withBody(expect.getReswithBody(expectationMap,candidate));
                    }
                    else if (candidate.compareToIgnoreCase("cookies") > 0 ) {
                        nextRes.withCookies(expect.getReswithCookies(expectationMap,candidate));
                    }
                    else if (candidate.compareToIgnoreCase("headers") > 0) {
                        nextRes.withHeaders(expect.getReswithHeaders(expectationMap,candidate));
                    }
                    else if (candidate.compareToIgnoreCase("response") > 0) {
                        nextRes.withStatusCode(expect.getReswithStatusCode(expectationMap,candidate));
                    }
                    exist = 0;
                }
            }

            if(exist == 0) {
                resQ.add(nextRes);
                count++;
            }
        }
        return resQ;
    }




    public HttpResponse readDefaultResponse (HashMap expectationMap) {

        HttpResponse newResponse = new HttpResponse();
        ExpectationElements expect = new ExpectationElements();

        if( expect.getReswithBody(expectationMap) != null) {
            newResponse.withBody(expect.getReswithBody(expectationMap));
        }

        if( expect.getReswithCookies(expectationMap) != null) {
            newResponse.withCookies(expect.getReswithCookies(expectationMap));
        }
        if(expect.getReswithHeaders(expectationMap) != null) {
            newResponse.withHeaders(expect.getReswithHeaders(expectationMap));
        }

        newResponse.withStatusCode(expect.getReswithStatusCode(expectationMap));
        newResponse.withDelay(expect.getResDelay(expectationMap));

        return newResponse;
    }
}
