package com.tester.mockall.instantiate;


import com.tester.mockall.mockserver.CallExpectation;

import org.apache.log4j.Logger;
import org.mockserver.integration.ClientAndServer;

import java.io.*;


/**
 * Created by vivek.agr on 12/3/14.
 */
public class Client extends Thread {

    static Logger logger = Logger.getLogger(Client.class.getName());
    private MainClass main = new MainClass();

    public void run() {
        logger.info("Running " + Thread.currentThread().getName());
        String fileName = Thread.currentThread().getName();
        String[] param = fileName.split(":");
        String expFile = param[0];
        int port = Integer.parseInt(param[1]);

       logger.info("Starting Expectation for "+ expFile + " on port number: "+port);

        Client mockserver = new Client();
        try {
            mockserver.startMockServer(expFile,port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        main.queue("I am thread number "+Thread.currentThread().getName()+" :All booted");
    }

    public ClientAndServer mockServer;

    Client () {
        this.mockServer = null;

    }
    static Logger log = Logger.getLogger(
            Client.class.getName());


    public void startMockServer(String filename, int port) throws Exception {

       ClientAndServer mockServices = ClientAndServer.startClientAndServer(port);

        try {
            CallExpectation expect = new CallExpectation();
            if (filename.endsWith(".csv")) {
            expect.readCSVForExpectations(mockServices,filename);
            } else if(filename.endsWith("yml")) {
                expect.readYAMLForExpectations(mockServices, filename);
            }

            logger.info("Expectations created for " + filename + " on port:" + port);

            //Using this statement will dump the requests in logfile but will take a lot of time to dump for long messages.
            //mockServices.dumpToLog();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void spawnMocks(String filename) throws Exception {
        /*
        Read the file with the expectation file and the port number on which the expectation needs to be created
         */
       logger.info("Reading configuration from :"+ filename);
        Thread t;
        int noOfMocksStarted =0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;

            while((line = br.readLine()) != null ) {
                //Do this with a thread
                t = new Thread(this,line);
                t.setName(line);
                t.start();
                noOfMocksStarted++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while ( main.internalList.size() != noOfMocksStarted ) {
            sleep(1000);
        }

        logger.info("Expectations created for all Mocks");

        while (true) {
            Thread.sleep(100000);
        }


    }



}
