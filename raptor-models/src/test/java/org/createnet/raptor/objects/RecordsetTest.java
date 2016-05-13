/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.models.data.IRecord;
import org.createnet.raptor.models.data.Record;
import org.createnet.raptor.models.data.types.BooleanRecord;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.exception.RecordsetException;
import org.createnet.raptor.models.objects.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class RecordsetTest extends TestUtils {
  
  JsonNode data;
  JsonNode data1;
  
  public RecordsetTest() {
    
  }
  
  @BeforeClass
  public static void setUpClass() {
    
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() throws IOException {
    loadObject();
    data = loadData("record");
    data1 = loadData("record1");
  }
  
  @After
  public void tearDown() {
    
  }

  /**
   * Test of parse method, of class ServiceObject.
   * @throws org.createnet.raptor.models.exception.RecordsetException
   * @throws com.fasterxml.jackson.core.JsonProcessingException
   */
  @Test
  public void testParseRecord() throws RecordsetException, JsonProcessingException, IOException {
    
    Stream stream = serviceObject.streams.get("mylocation");
    RecordSet records = mapper.readValue(data.toString(), RecordSet.class);
            
    IRecord channel = records.getByChannelName("happy");
    assertTrue(channel instanceof BooleanRecord);
    assertTrue(channel.getValue().equals(true));
    
    System.out.println("org.createnet.raptor.objects.RecordsetTest.testParseRecord() "  + records.toJson()); 
    
  }
  
  @Test
  public void testParseStreamData() throws RecordsetException, JsonProcessingException, IOException {
    
    RecordSet records = mapper.readValue(data1.toString(), RecordSet.class);
    
    System.out.println("org.createnet.raptor.objects.RecordsetTest.testParseRecord() "  + records.toJson()); 
    
  }

}
