/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import io.atomix.Atomix;
import io.atomix.AtomixReplica;
import io.atomix.AtomixServer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Cluster class for managing the Raft replicas and cluster managers 
 * @author muthukumarsuresh
 */
public class RaftCluster {
    static int portId = 5000;
    public static class ServerSetup{
       ServerSetup(List<Address> members, Address address) throws InterruptedException{
           
        AtomixServer.Builder builder = AtomixServer.builder(address, members).withTransport(new NettyTransport())
        .withStorage(new Storage(StorageLevel.DISK));   
        AtomixServer server = builder.build();
        System.out.println("whattt");
        CompletableFuture<AtomixServer> future = server.open();
        future.thenRun(() -> {
            System.out.println("Server started!"+server.toString() + "   " + server.toString() + "    " + server.isOpen());      
        }); 
        server.close();
        future.complete(server);
        future.join();
       } 
       

    } 
    /**
     * creates the cluster 
     * @param nodesInCluster
     * @param numberOfPartitions
     * @throws InterruptedException 
     */
    public static void createCluster(int nodesInCluster, int numberOfPartitions) throws InterruptedException{
        int startingPort = portId;
        List<Address> members = Arrays.asList(
            new Address("localhost", portId++),
            new Address("localhost", portId++),
            new Address("localhost", portId++)
        );
        List<ServerSetup> servers = new ArrayList<ServerSetup>();
        
        for(int i = 0; i < nodesInCluster; i++){
            servers.add(new ServerSetup(members, new Address("localhost", startingPort+i)));
        }
    }
    
}
