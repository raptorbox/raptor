/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
import org.createnet.raptor.models.objects.serializer.ServiceObjectView;
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
  public void testParse() throws RaptorComponent.ParserException {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    assertTrue(serviceObject.name.equals("Phone"));
    
    assertTrue(serviceObject.streams.size() == 1);
    assertTrue(serviceObject.streams.get("mylocation").channels.get("position").type.toLowerCase().equals("geo_point"));
    
    assertTrue(serviceObject.actions.size() == 3);
    assertTrue(serviceObject.actions.get("makeCall") != null);
    
  }
  
  @Test
  public void testSerializeToJsonNode() throws RaptorComponent.ParserException {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    JsonNode node = serviceObject.toJsonNode();
    
    assertTrue(node.has("name"));
    
  }
  
  @Test
  public void testSerializeToJsonNodeIdOnly() throws RaptorComponent.ParserException {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    JsonNode node = serviceObject.toJsonNode(ServiceObjectView.IdOnly);
    
    assertFalse(node.has("name"));
    
  }
  
  
  @Test
  public void testSerializeViewPublic() throws RaptorComponent.ParserException, IOException {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    String strjson = serviceObject.toJSON(ServiceObjectView.Public);
    JsonNode json = mapper.readTree(strjson);
    
    assertFalse(json.has("userId"));
  }
  
  @Test
  public void testSerializeViewInternal() throws RaptorComponent.ParserException, IOException {
    
    serviceObject.parse(jsonServiceObject.toString());
    
    String strjson = serviceObject.toJSON(ServiceObjectView.Internal);
    JsonNode json = mapper.readTree(strjson);
    
    assertTrue(json.has("userId"));
  }
  
  @Test
  public void testSerializeViewIdOnly() throws RaptorComponent.ParserException, IOException {
    
    ObjectNode jsonObj = (ObjectNode) jsonServiceObject;
    jsonObj.put("id", UUID.randomUUID().toString());
    
    serviceObject.parse(jsonObj.toString());
    
    String strjson = serviceObject.toJSON(ServiceObjectView.IdOnly);
    JsonNode json = mapper.readTree(strjson);

    assertTrue(json.has("id"));    
    assertFalse(json.has("userId"));
    assertFalse(json.has("name"));

  }

  
  /**
   * Test of isNew method, of class ServiceObject.
   */
  @Test
  public void testIsNew() {
    assertTrue((new ServiceObject()).isNew());
  }

}
