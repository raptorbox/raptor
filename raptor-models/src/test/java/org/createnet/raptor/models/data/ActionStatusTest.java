/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.raptor.models.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
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
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class ActionStatusTest extends TestUtils {
  
  public ActionStatusTest() {
    
  }
  
  @BeforeClass
  public static void setUpClass() {
    
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() throws IOException {
  }
  
  @After
  public void tearDown() {
    
  }

  @Test
  public void testParseActionStatus() throws IOException  {
    
    JsonNode json = loadData("actionStatus");
    
    ActionStatus status = ActionStatus.parseJSON(json.toString());
    
    String statusPublic = status.toJSON();
    JsonNode statusPublicJson = ServiceObject.getMapper().readTree(statusPublic);
    
    assertTrue(statusPublicJson.has("actionId"));
    
  }

}
