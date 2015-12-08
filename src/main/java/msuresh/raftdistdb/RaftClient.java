/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import static io.netty.util.CharsetUtil.US_ASCII;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.json.simple.JSONObject;
import java.lang.*;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author muthukumarsuresh
 */
public class RaftClient {
    public static void SetValue(String name, String key, String value) throws FileNotFoundException {
        if (key == null || key.isEmpty()) {
            return;
        }
        File configFile = new File(Constants.STATE_LOCATION + name + ".info");
        if (!configFile.exists() || configFile.isDirectory()) {
            FileNotFoundException ex = new FileNotFoundException();
            throw ex;
        }
        try {
            System.out.println("Adding key .. hold on..");
            String content = new Scanner(configFile).useDelimiter("\\Z").next();
            JSONObject config = (JSONObject) (new JSONParser()).parse(content);
            Long numPart = (Long) config.get("countPartitions");
            Integer shardId = key.hashCode() % numPart.intValue();
            JSONArray memberJson = (JSONArray) config.get(shardId.toString());
            List<Address> members = new ArrayList<>();
            for (int i = 0; i < memberJson.size(); i++) {
                JSONObject obj = (JSONObject) memberJson.get(i);
                Long port = (Long) obj.get("port");
                String address = (String) obj.get("address");
                members.add(new Address(address, port.intValue()));
            }
            CopycatClient client = CopycatClient.builder(members)
                    .withTransport(new NettyTransport())
                    .build();
            client.open().join();
            client.submit(new PutCommand(key, value)).get();
            client.close().join();
            while (client.isOpen()) {
                Thread.sleep(1000);
            }
            System.out.println("key " + key + " with value : " + value + " has been added to the cluster");
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    public static void GetValue(String name, String key) throws FileNotFoundException {
        if (key == null || key.isEmpty()) {
            return;
        }
        File configFile = new File(Constants.STATE_LOCATION + name + ".info");
        if (!configFile.exists() || configFile.isDirectory()) {
            FileNotFoundException ex = new FileNotFoundException();
            throw ex;
        }
        try {
            System.out.println("Getting key .. Hold on.");
            String content = new Scanner(configFile).useDelimiter("\\Z").next();
            JSONObject config = (JSONObject) (new JSONParser()).parse(content);
            Long numPart = (Long) config.get("countPartitions");
            Integer shardId = key.hashCode() % numPart.intValue();
            JSONArray memberJson = (JSONArray) config.get(shardId.toString());
            List<Address> members = new ArrayList<>();
            for (int i = 0; i < memberJson.size(); i++) {
                JSONObject obj = (JSONObject) memberJson.get(i);
                Long port = (Long) obj.get("port");
                String address = (String) obj.get("address");
                members.add(new Address(address, port.intValue()));
            }
            CopycatClient client = CopycatClient.builder(members)
                    .withTransport(new NettyTransport())
                    .build();
            client.open().join();
            Object str = client.submit(new GetQuery(key)).get();
            System.out.println("For the key : "+ key + ", the database returned the value : " + (String) str);
            client.close().join();
            while (client.isOpen()) {
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
