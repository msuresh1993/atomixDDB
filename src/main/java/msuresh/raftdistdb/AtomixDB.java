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
 *
 * @author muthukumarsuresh
 */
public class AtomixDB {
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

    private static void setupServers(int noOfServers, int noOfReplicas) throws InterruptedException {
        RaftCluster.createCluster(noOfServers, noOfReplicas);
        
    }

    private static void addKey(String key, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String addKey(String parseInt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
