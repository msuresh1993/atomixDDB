/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msuresh.raftdistdb;

import java.io.FileWriter;
import org.json.simple.JSONObject;

/**
 *
 * @author muthukumarsuresh
 */
public class JSONString {
    public static void ConvertJSON2File(JSONObject json, String floc){
        try (FileWriter file = new FileWriter(floc)) {
			file.write(json.toJSONString());
            }
        catch(Exception ex){
            
        }
        
    }
}
