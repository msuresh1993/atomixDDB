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
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author muthukumarsuresh
 */
public class testClass {
    
    public static class ServerSetup{
       ServerSetup(List<Address> members, Address address) throws InterruptedException{
           

        AtomixReplica replica = AtomixReplica.builder(address, members)
        .withTransport(new NettyTransport())
        .withStorage(new Storage(StorageLevel.DISK))
        .build();
        System.out.println("whattt");
        CompletableFuture<Atomix> future = replica.open();
        future.thenRun(() -> {
            System.out.println("Replica started!"+replica.toString() + "   " + address.toString() + "    " + replica.isOpen());      
        }); 
        
       } 
       

    }
    public static void main(String[] args) throws InterruptedException{
        List<Address> members = Arrays.asList(
            new Address("localhost", 5000),
            new Address("localhost", 5001),
            new Address("localhost", 5002)
        );
        List<ServerSetup> servers = new ArrayList<ServerSetup>();
        
        for(int i = 0; i < 3; i++){
            System.out.println(i);
            servers.add(new ServerSetup(members, new Address("localhost", 5000+i)));
        }
    }
}


//<dependency>
//        <groupId>io.atomix</groupId>
//        <artifactId>atomix-all</artifactId>
//        <version>0.1.0-beta4</version>
//    </dependency>
//    <dependency>
//      <groupId>org.mod4j.org.apache.commons</groupId>
//      <artifactId>lang</artifactId>
//      <version>[2.0.0,3.0.0)</version>
//    </dependency>
//    <dependency>
//      <groupId>org.mod4j.org.apache.commons</groupId>
//      <artifactId>logging</artifactId>
//      <version>[1.0.0,2.0.0)</version>
//    </dependency>