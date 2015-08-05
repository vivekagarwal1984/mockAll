package com.tester.mockall.instantiate;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek.agr on 1/12/15.
 */
public class MainClass{

    final static Logger logger = Logger.getLogger(MainClass.class);
    public static  String configFile;
    public static List<String> internalList = new ArrayList<String>();

    public void queue(String s)
    {
        internalList.add(s);
    }

    static class Message extends Thread {
        public void run() {
            logger.info("Can we clean all the child process here? How?.");
        }
    }

    public static void main(String[] args) throws Exception {

        logger.info("Starting Mockserver!");
        Runtime.getRuntime().addShutdownHook(new Message());
        if((args.length == 1) || (args.length == 2)) {

            System.out.println("Log file will be created at /var/log/flipkart/mockserver/logs/mockserver.log");
            // By default log it in INFO mode

            String mode="-info";
            if(args.length == 2) {
                mode = args[1];
            }

            switch(mode) {
                case "-debug":
                    System.out.println("Logging in DEBUG mode");
                    LogManager.getRootLogger().setLevel(Level.DEBUG);
                    break;
                case "-warn":
                    System.out.println("Logging in WARN mode");
                    LogManager.getRootLogger().setLevel(Level.WARN);
                    break;
                default :
                    System.out.println("Logging in INFO mode");
                    LogManager.getRootLogger().setLevel(Level.INFO);
                    break;
            }

            Client clnt = new Client();
            configFile = args[0];

            clnt.spawnMocks(configFile);

        } else {
            System.out.println("Proper Usage for INFO mode is: java -jar <jar-file-name> config.conf");
            System.out.println("Proper Usage for DEBUG mode is: java -jar <jar-file-name> config.conf -debug");
            System.out.println("config.conf should have <expectation-file>:<port-no>");
            logger.info("Proper Usage is: java -jar <jar-file-name> config.conf");

            System.exit(0);
        }

    }
}
