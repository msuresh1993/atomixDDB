/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import io.atomix.Atomix;
import io.atomix.AtomixClient;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author muthukumarsuresh
 */
public class Client {
    public static void main(String [] args)
    {
        
//        Address address = new Address("123.456.789.0", 5000);
//
//        List<Address> members = Arrays.asList(
//            new Address("localhost", 5000),
//            new Address("localhost", 5000),
//            new Address("localhost", 5000)
//        );
//        Atomix atomix = AtomixClient.builder()
//  .withTransport(new NettyTransport())
//  .withMembers(members.builder()
//    .addMember(new Member(1, "123.456.789.1", 5555))
//    .addMember(new Member(2, "123.456.789.2", 5555))
//    .addMember(new Member(3, "123.456.789.3", 5555))
//    .build())   
//  .build();
//        atomix.open().thenRun(() -> {
//  System.out.println("Client connected!");
//});
    }
}
