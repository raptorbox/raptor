/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.UUID;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.utils.TestUtils;
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
public class ServiceObjectTest extends TestUtils {
  
  protected JsonNode jsonData;
  
  public ServiceObjectTest() {
    
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
    jsonData = loadData("record");
  }
  
  @After
  public void tearDown() {
    
  }

  /**
   * Test of parse method, of class ServiceObject.
   * @throws org.createnet.raptor.models.objects.RaptorComponent.ParserException
   */
  @Test
  public void testParse() {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    assertTrue(serviceObject.name.equals("Phone"));
    
    assertTrue(serviceObject.streams.size() == 1);
    assertTrue(serviceObject.streams.get("mylocation").channels.get("position").type.toLowerCase().equals("geo_point"));
    
    assertTrue(serviceObject.actions.size() == 3);
    assertTrue(serviceObject.actions.get("makeCall") != null);
    
  }
  
  @Test
  public void testSerializeToJsonNode()  {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    JsonNode node = serviceObject.toJsonNode();
    
    assertTrue(node.has("name"));
    
  }
  
  @Test
  public void testSerializeViewPublic() throws IOException {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    String strjson = serviceObject.toJSON();
    JsonNode json = mapper.readTree(strjson);
    
    assertTrue(json.has("userId"));
    assertTrue(json.has("parentId"));
  }
  
  /**
   * Test of isNew method, of class ServiceObject.
   */
  @Test
  public void testIsNew() {
    assertTrue((new ServiceObject()).isNew());
  }


}
