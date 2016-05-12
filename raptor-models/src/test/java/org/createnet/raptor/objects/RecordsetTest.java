/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.createnet.raptor.models.data.BooleanRecord;
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
    loadData();
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
  public void testParseRecord() throws RecordsetException, JsonProcessingException {
    
    Stream stream = serviceObject.streams.get("mylocation");
    RecordSet records = new RecordSet(stream, jsonData);
    
    assertTrue(records.getByChannelName("happy") instanceof BooleanRecord);
    assertTrue(records.getByChannelName("happy").getValue().equals(true));
    
    System.out.println("org.createnet.raptor.objects.RecordsetTest.testParseRecord() "  + records.toJson()); 
    
  }

}
