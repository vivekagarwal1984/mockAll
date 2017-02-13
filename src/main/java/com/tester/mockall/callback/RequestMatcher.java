package com.tester.mockall.callback;

import com.tester.mockall.mockserver.ExpectationElements;
import org.apache.log4j.Logger;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vivek.agr on 2/23/15.
 */
public class RequestMatcher {

    static Logger logger = Logger.getLogger(
            RequestMatcher.class);

    public static int checkRequestMatch(HttpRequest request, HashMap expectationMap) {

        ExpectationElements expect = new ExpectationElements();

        if ((compareRegexAndSimple(request.getMethod(), expect.getReqwithMethod(expectationMap))) != 0) {
            logger.debug("Actual:" + request.getMethod() + ", Expected:" + expect.getReqwithMethod(expectationMap));
            logger.debug("Method does not match");
            return 1;
        }

        if((compareRegexAndSimple(request.getPath(),expect.getReqwithPath(expectationMap))) != 0) {
            logger.debug("Actual:" + request.getPath() + ", Expected:" + expect.getReqwithPath(expectationMap));
            logger.debug("Path does not match");
            return 1;
        }

        if(expect.getReqwithQueryStringParameter(expectationMap) != null) {
            if((compareParametersRegexAndSimple(request.getQueryStringParameters(), expect.getReqwithQueryStringParameter(expectationMap))) != 0) {
                logger.debug("Actual:" + request.getQueryStringParameters() + ", Expected:" + expect.getReqwithQueryStringParameter(expectationMap));
                logger.debug("Query String Param does not match");
                return 1;
            }
        }

        if((expect.getReqwithHeaders(expectationMap)) != null) {
            if ((compareHeadersRegexAndSimple(request.getHeaders(), expect.getReqwithHeaders(expectationMap))) != 0) {
                logger.debug("Actual:" + request.getHeaders() + ", Expected:" + expect.getReqwithHeaders(expectationMap));
                logger.debug("Headers does not match");
                return 1;
            }
        }

        if(expect.getReqwithBody(expectationMap) != null) {
            if ((compareRegexAndSimple(request.getBodyAsString(), expect.getReqwithBody(expectationMap).toString())) != 0) {
                logger.debug("Actual:" + request.getBodyAsString() + ", Expected:" + expect.getReqwithBody(expectationMap));
                logger.debug("Body does not match");
                return 1;
            }
        }

        logger.debug("Returning All match for ");
        logger.debug ("request : " + request);
        logger.debug("expectationMap : " + expectationMap);
        return 0;
    }


    /*
    This function will compare the actual and expected parameters of a HttpRequest
    It will send 0 for both a regex or a exact match
     */

    public static int compareRegexAndSimple (String actual, String expected) {
        //Exact Comparison
        if(( actual.equalsIgnoreCase(expected)) || (expected == null))
            return 0;

        if((expected.equals("") && !actual.equals("")) || (!expected.equals("") && actual.equals("")))
            return 1;

        //Regex Comparison only if expected has regex=
        //if (expected.startsWith("regex=")) {
         //   expected = expected.substring(6);


            Pattern p = Pattern.compile(expected);
            Matcher m = p.matcher(actual);
            if (m.matches())
                return 0;
       // }

        return 1;

    }

    public static int compareParametersRegexAndSimple (List<Parameter> actual, List<Parameter> expected)
    {
        logger.debug("Inside compareParametersRegexAndSimple");
        System.out.println("Actual = " + actual.toString());
        System.out.println("Expected = "+ expected.toString());

        try
        {
            if(expected == null)
                return 0;

            if (actual.isEmpty())
                return 1;

            boolean keyMatch;
            for(Parameter exp: expected) {
                String eKey = exp.getName();
                List<String> eValues = exp.getValues();

                keyMatch = false;
                for (Parameter av : actual) {
                    String aKey = av.getName();
                    List<String> aValues = av.getValues();

                    Pattern pattern = Pattern.compile(eKey);
                    Matcher matcher = pattern.matcher(aKey);

                    if (matcher.matches())//Regex match
                    {
                        keyMatch = true;
                        for (String eValue : eValues) {
                            boolean flag = false;
                            for (String aValue : aValues) {
                                pattern = Pattern.compile(eValue);
                                matcher = pattern.matcher(aValue);
                                if (matcher.matches()) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag) {
                                return 1;
                            }
                        }
                    }
                }

                if (!keyMatch) {
                    return 1;
                }
            }

            return 0;
        }
        catch (NullPointerException e)
        {
            return 1;
        }
    }

    public static int compareHeadersRegexAndSimple (List<Header> actual, List<Header> expected) {

        if(expected == null)
            return 0;

        if (actual.isEmpty())
            return 1;

        boolean keyMatch = false;
        for(Header exp: expected)
        {
            String eKey = exp.getName();
            List<String> eValues = exp.getValues();

            for(Header av: actual)
            {
                String aKey = av.getName();
                List<String> aValues = av.getValues();

                Pattern pattern = Pattern.compile(eKey);
                Matcher matcher = pattern.matcher(aKey);

                if (matcher.matches())//Regex match
                {
                    keyMatch = true;
                    for(String eValue: eValues)
                    {
                        boolean flag = false;
                        for(String aValue: aValues)
                        {
                            pattern = Pattern.compile(eValue);
                            matcher = pattern.matcher(aValue);
                            if (matcher.matches())
                            {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag)
                        {
                            return 1;
                        }
                    }
                }
            }
        }
        if (!keyMatch)
        {
            return 1;
        }

        return 0;
    }

}
