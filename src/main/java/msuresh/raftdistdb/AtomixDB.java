/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The main driver class for AtomixDDB.
 * @author muthukumarsuresh
 */
public class AtomixDB {
    /**
     * Main Method which runs a Apache CLI which takes 3 different options for the 3 operations <br>
     * @param args <br>
     * Options : <br>
     * -setup numberOfReplicas numberOfPartitions -- which sets the system given thenumber of partitions number of replicas per partition<br>
     * -set key Value -- adds a key value pair to the DB<br>
     * -get key -- returns a value for the key if it exists<br>
     * @throws InterruptedException 
     */
    public static void main(String [] args) throws InterruptedException{
        Options options = new Options();
        Option opt = new Option("setup", true, "Sets up the replica that run the Raft Consensus Algorithm." );
        opt.setArgs(2);
        options.addOption(opt);
        opt = new Option("set", true, " Add a key-value pair into the Distributed Database.");
        opt.setArgs(2);
        options.addOption(opt);
        opt = new Option("get", true, "Given a key gets the value from the DB");
        opt.setArgs(2);
        options.addOption(opt);
    try{
        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        line = parser.parse(options, args);
        if(line.hasOption("setup")){
            String[] vals = line.getOptionValues("setup");
            setupServers(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
        }
        else if(line.hasOption("set")){
            String[] vals = line.getOptionValues("set");
            addKey(vals[0], vals[1]);
        }
        else if(line.hasOption("get")){
            String[] vals = line.getOptionValues("get");
            addKey(vals[0]);
        }
    }catch( ParseException exp ) {
        System.out.println( "Unexpected exception:" + exp.getMessage() );
    }
    }
    /**
     * Method to setup server. Internally calls RaftCluster to setup the cluster with replicas and partition managers
     * @param noOfServers
     * @param noOfReplicas
     * @throws InterruptedException 
     */
    private static void setupServers(int noOfServers, int noOfReplicas) throws InterruptedException {
        RaftCluster.createCluster(noOfServers, noOfReplicas);
        
    }
    /**
     * adds key to DB
     * @param key
     * @param value 
     */
    private static void addKey(String key, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * returns a value for the key if it exists
     * @param parseInt
     * @return String
     */
    private static String addKey(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
