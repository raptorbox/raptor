/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.ServiceObject;
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
    loadData();
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

  
  /**
   * Test of isNew method, of class ServiceObject.
   */
  @Test
  public void testIsNew() {
    assertTrue((new ServiceObject()).isNew());
  }

}
