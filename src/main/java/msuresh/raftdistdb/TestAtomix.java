/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author muthukumarsuresh
 */
public class TestAtomix {

    private static int portId;

    public static class ServerSetup extends Thread {

        private List<Address> members;
        private Address address;
        private Map atomixList;
        CompletableFuture<Integer> future;

        ServerSetup(List<Address> members, Address address, Map atomixList, CompletableFuture<Integer> future) throws InterruptedException, ExecutionException {
            this.members = members;
            this.address = address;
            this.atomixList = atomixList;
            this.future = future;
        }

        public void run() {
            CopycatServer atomix = CopycatServer.builder(address, members)
                    .withTransport(new NettyTransport())
                    .withStateMachine(new MapStateMachine())
                    .withStorage(Storage.builder()
                            .withDirectory(System.getProperty("user.dir") + "/logs/" + UUID.randomUUID().toString())
                            .build())
                    .build();
            atomixList.put(address, atomix);
            atomix.open().join();
//           DistributedMembershipGroup group = atomix.create("group", DistributedMembershipGroup::new).get();
            future.complete(42);

//           leaderFailure(atomix);
        }

    }

    public static class LeaderReelection extends Thread {

        Address address;
        CopycatServer atomix;
        boolean close;
        CompletableFuture<Integer> future = new CompletableFuture<>();
        LeaderReelection(Address address, CopycatServer atomix, boolean close) {
            this.address = address;
            this.atomix = atomix;
            this.close = close;
        }

        public void run() {
            Address leader = atomix.leader();
            if(close)
                System.out.println("At host :" + address + ", initial leader :" + leader);
            while (true) {
                if (address.port() == leader.port()) {
                    if(close)
                        System.out.println("Crashing leader at :" + address);
                    atomix.close();
                    break;
                } else {
                    if (close) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RaftCluster.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.out.println("At host :" + address + ", after reelection :" + atomix.leader());
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(TestAtomix.class.getName()).log(Level.SEVERE, null, ex);
//                    }

                        System.out.println("Closed server at" + address);
                        atomix.close();
                    }
                    break;
                }
            }
            future.complete(21);
        }
    }

    public static void createCluster(String test, int nodesInCluster) throws InterruptedException, ExecutionException {
        InitPortNumber();
        try {
            List<Address> members = new ArrayList<>();
            for (int i = 0; i < nodesInCluster; i++) {
                Address addr = new Address("localhost", portId++);
                members.add(addr);
            }
            CompletableFuture<Integer> future = new CompletableFuture<>();
            Map atomixList;
            atomixList = new HashMap();
            for (Address a : members) {
                ServerSetup s = new ServerSetup(members, a, atomixList, future);
                s.start();
            }
            future.get();
            UpdatePortNumber();
            
            if (test.compareTo("leaderFailure") == 0) {
                for (Object s : atomixList.keySet()) {
                    LeaderReelection l = new LeaderReelection((Address) s, (CopycatServer) atomixList.get(s), true);
                    l.start();
                }
                Thread.sleep(20000);
//                for(Object s : atomixList.keySet()){
//                    CopycatServer cs = (CopycatServer)atomixList.get(s);
//                    while(cs.isOpen())
//                        Thread.sleep(1000);
//                    System.out.println("printing" + cs.toString());
//                }
                System.out.println("Leader Reelection test is done. Program might not close properly due to a bug in Atomix. Follow manual instructions to close the process and sockets.");
                } else if (test.compareTo("replicationTest") == 0) {
                CopycatClient client = CopycatClient.builder(members)
                        .withTransport(new NettyTransport())
                        .build();
                client.open().join();
                System.out.println("Adding a testkey with testval to the cluster ..");
                client.submit(new PutCommand("testkey1", "testval")).get();
                List<LeaderReelection> reelectionList = new ArrayList<>();
                System.out.println("Crashing leader to trigger a reelection .. ");
                for (Object s : atomixList.keySet()) {
                    
                    LeaderReelection l = new LeaderReelection((Address) s, (CopycatServer) atomixList.get(s), false);
                    l.start();
                    reelectionList.add(l);
                }
                
//                for(LeaderReelection l : reelectionList){
//                    l.future.get();
//                    
//                }
//                client = CopycatClient.builder(members)
//                        .withTransport(new NettyTransport())
//                        .build();
//                client.open().join();
                System.out.println(" Polling the cluster for testkey ..");
                Object str = client.submit(new GetQuery("testkey1")).get();
                System.out.println("The cluster returned (which should be 'testval'):" + (String)str);
                System.out.println("closing open servers..");
                for(Object s : atomixList.keySet()){
                    CopycatServer cs = (CopycatServer)atomixList.get(s);
                    
                    if(cs.isOpen())
                        cs.close();
                    
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

    }

    private static void InitPortNumber() {
        try {
            File f= new File(Constants.STATE_LOCATION + "global.info");
            if(!f.exists())
            {
                RaftCluster.createDefaultGlobal();
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
            jsonObject.put("currentCount", portId);
            try (FileWriter file = new FileWriter(Constants.STATE_LOCATION + "global.info")) {
                file.write(jsonObject.toJSONString());
            }
        } catch (Exception e) {

        }
    }
}
