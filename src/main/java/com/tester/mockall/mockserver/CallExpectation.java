package com.tester.mockall.mockserver;

import com.csvreader.CsvReader;
import org.apache.log4j.Logger;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testng.util.Strings;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by vivek.agr on 12/3/14.
 */
public class CallExpectation {


    public static final String callbackClassName = "com.tester.mockall.callback.HandleMultipleResponse";
    static Logger logger = Logger.getLogger(
            CallExpectation.class.getName());


    /* This section will read the values from the csv files which is used to create Expectations

     */

    public void readCSVForExpectations(ClientAndServer mockServer, String filename) throws Exception {

        HashMap expectationmap = new HashMap();

        CsvReader expectations = new CsvReader(filename, '#');
        expectations.readHeaders();
        while (expectations.readRecord()) {
                for (int i = 0; i < expectations.getHeaderCount(); i++) {
                    if (expectations.get(expectations.getHeader(i)) != "") {
                        expectationmap.put(expectations.getHeader(i), expectations.get(expectations.getHeader(i)));
                    }
                }
                logger.debug("Dumping the expectation for " + filename + " : " + expectationmap.get("testid"));
                logger.debug(expectationmap.toString());

                logger.debug("---------------------------------------------------------");
            generateExpectations(expectationmap, mockServer);
        }
    }

    public void readYAMLForExpectations(ClientAndServer mockServer, String filename) throws Exception {

   // public static void readYAMLForExpectations(String filename) throws Exception {

        Yaml yaml = new Yaml();
        InputStream input = new FileInputStream(new File(filename));
        Object data = yaml.load(input); // load the yaml document into a java object
        HashMap<String,HashMap> map = (HashMap<String,HashMap>) data;

        for(Map.Entry<String, HashMap> e: map.entrySet()){
            HashMap expectationmap =new HashMap();
            Iterator it = e.getValue().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                expectationmap.put(pair.getKey(), pair.getValue());
                it.remove();
            }

            generateExpectations(expectationmap, mockServer);
        }
    }

    public void generateExpectations(HashMap<String,String> expectations,ClientAndServer mockServer) {

        //Generate the expected request
        ExpectationElements ele = new ExpectationElements();
        HttpRequest exRequest = new HttpRequest();

        if (ele.getReqwithBody(expectations) != null)
            exRequest.withBody(ele.getReqwithBody(expectations));

        if (ele.getReqwithCookies(expectations) != null)
            exRequest.withCookies(ele.getReqwithCookies(expectations));

        if (ele.getReqwithHeaders(expectations) != null)
            exRequest.withHeaders(ele.getReqwithHeaders(expectations));

        if (ele.getReqwithMethod(expectations) != null)
            exRequest.withMethod(ele.getReqwithMethod(expectations));

        if (ele.getReqwithPath(expectations) != null)
            exRequest.withPath(ele.getReqwithPath(expectations));

        if (ele.getReqwithQueryStringParameter(expectations) != null)
            exRequest.withQueryStringParameters(ele.getReqwithQueryStringParameter(expectations));

        //Generate the expected response
        HttpResponse exResponse = new HttpResponse();

        if (ele.getReswithBody(expectations) != null)
            exResponse.withBody(ele.getReswithBody(expectations));

        if (ele.getReswithCookies(expectations) != null)
            exResponse.withCookies(ele.getReswithCookies(expectations));

        if (ele.getReswithHeaders(expectations) != null)
            exResponse.withHeaders(ele.getReswithHeaders(expectations));

//        if (ele.getReswithStatusCode(expectations) )
//            exResponse.withStatusCode(ele.getReswithStatusCode(expectations));

        if (ele.getResDelay(expectations) != null) {
            exResponse.withDelay(ele.getResDelay(expectations));

        }

        if(! Strings.isNullOrEmpty(expectations.get("callbackClass"))) {
            Expectation.createCallbackwithClass(mockServer, exRequest, callbackClassName);
        } else {
           // Expectation.createExpectation(mockServer, exRequest, exResponse);
            Expectation.createCallbackwithClass(mockServer, exRequest, callbackClassName);
        }
    }


}