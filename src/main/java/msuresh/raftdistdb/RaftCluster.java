/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import io.atomix.Atomix;
import io.atomix.AtomixClient;
import io.atomix.AtomixReplica;
import io.atomix.AtomixServer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.coordination.DistributedMembershipGroup;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Cluster class for managing the Raft replicas and cluster managers 
 * @author muthukumarsuresh
 * 
 */
public class RaftCluster {
    private static int portId;

    public static void createDefaultGlobal() throws IOException {
        JSONObject obj = new JSONObject();
        obj.put("currentCount",5000);
        try (FileWriter file = new FileWriter(Constants.STATE_LOCATION + "global.info")) {
			file.write(obj.toJSONString());
        }
    }
//    static boolean flag = false;
    public static class ServerSetup extends Thread{
       private List<Address> members;
       private Address address;
       private List<CopycatServer> atomixList;
       CompletableFuture<Integer> future;
       ServerSetup(List<Address> members, Address address, List<CopycatServer> atomixList,CompletableFuture<Integer> future) throws InterruptedException, ExecutionException{
           this.members = members;
           this.address = address;
           this.atomixList = atomixList;
           this.future = future;
       } 
       public void run(){
           CopycatServer atomix = CopycatServer.builder(address, members)
            .withTransport(new NettyTransport())
            .withStateMachine(new MapStateMachine())
            .withStorage(Storage.builder()
            .withDirectory(System.getProperty("user.dir") + "/logs/" + UUID.randomUUID().toString())
            .build())
            .build();
           atomixList.add(atomix);
           atomix.open().join();
//           DistributedMembershipGroup group = atomix.create("group", DistributedMembershipGroup::new).get();
           future.complete(42);
        }
    }
       
    
    /**
     * creates the cluster 
     * @param nodesInCluster
     * @param numberOfPartitions
     * @throws InterruptedException 
     */
    public static void createCluster(String name, int nodesInCluster, int numberOfPartitions) throws InterruptedException, ExecutionException{
        InitPortNumber();
        try{
            JSONObject cluster = new JSONObject();
            cluster.put("name", name);
            cluster.put("countReplicas", nodesInCluster);
            cluster.put("countPartitions", numberOfPartitions);
            
            for(int i =0; i < numberOfPartitions; i++)
                cluster.put(i,CreatePartitionCluster(nodesInCluster));
            JSONString.ConvertJSON2File(cluster, Constants.STATE_LOCATION + name + ".info");
            UpdatePortNumber();
            System.out.println("Database " + name + " has been created. ");
            
        }catch(Exception ex){
            System.out.println(ex.toString());
        }
        
    }

    private static JSONArray CreatePartitionCluster(int numReplicas) throws ExecutionException, InterruptedException {
        JSONArray arr = new JSONArray();
        JSONObject[] lis = new JSONObject[numReplicas];
        List<Address> members = new ArrayList<>();
        SetupServerAddress(numReplicas, lis, members, arr);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        List<CopycatServer> atomixList = new ArrayList<>();
        for(Address a : members){
            ServerSetup s = new ServerSetup(members, a, atomixList, future);
            s.start();
        }
        future.get();
        return arr;
    }

    private static void SetupServerAddress(int numReplicas, JSONObject[] lis, List<Address> members, JSONArray arr) {
        for(int i = 0; i < numReplicas; i++){
            Address addr = new Address("localhost", portId++);
            lis[i] = new JSONObject();
            lis[i].put("address", addr.host());
            lis[i].put("port", addr.port());
            members.add(addr);
            arr.add(lis[i]);
        }
    }

    public static void InitPortNumber() {
        try {
            File f= new File(Constants.STATE_LOCATION + "global.info");
            if(!f.exists())
            {
                createDefaultGlobal();
            }
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(Constants.STATE_LOCATION + "global.info"));
            JSONObject jsonObject = (JSONObject) obj;
            Long a = (Long) jsonObject.get("currentCount");
            portId = a.intValue();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    private static void UpdatePortNumber() {
        try {
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(Constants.STATE_LOCATION + "global.info"));
            JSONObject jsonObject = (JSONObject) obj;
            jsonObject.put("currentCount",portId);
            try (FileWriter file = new FileWriter(Constants.STATE_LOCATION + "global.info")) {
			file.write(jsonObject.toJSONString());
            }
        } catch (Exception e) {
            
        }
    }
}
