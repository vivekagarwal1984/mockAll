package com.tester.mockall.mockserver; /**
 * Created by vivek.agr on 12/3/14.
 */


import org.apache.log4j.Logger;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.Deque;
import java.util.HashMap;

public class Expectation {

    //Create a Hashmap to be used for callback classes
    public static HashMap<HashMap, Deque<HttpResponse>> reqToMulResponse = new HashMap<HashMap, Deque<HttpResponse>>();

    static Logger logger = Logger.getLogger( Expectation.class.getName());


    public static void createCallbackwithClass(ClientAndServer mockServer, HttpRequest request, String classname) {
        HttpCallback callback = new HttpCallback();
        logger.debug("Inside createCallbackwithClass");
        logger.debug(request.toString());
        mockServer.dumpToLog()
                .when(
                        request
                )
                .callback(callback.withCallbackClass(classname));

    }


    public static void createExpectation(ClientAndServer mockServer, HttpRequest request, HttpResponse response) {
        HttpCallback callback = new HttpCallback();
        logger.debug("Inside createExpectation");
        logger.debug(request.toString());
        mockServer.dumpToLog()
                .when(
                        request
                )
                .respond(
                        response
                );

    }

}
