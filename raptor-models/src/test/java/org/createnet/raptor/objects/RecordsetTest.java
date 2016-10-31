
package org.createnet.raptor.objects;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.createnet.raptor.models.data.IRecord;
import org.createnet.raptor.models.data.types.BooleanRecord;
import org.createnet.raptor.models.data.RecordSet;
import org.createnet.raptor.models.data.ResultSet;
import org.createnet.raptor.models.objects.RaptorComponent;
import org.createnet.raptor.models.objects.Stream;
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
  public void setUp() {
    loadObject();    
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
  public void testParseRecord() throws IOException {
    
    JsonNode data = loadData("record");
    
    Stream stream = serviceObject.streams.get("mylocation");
    RecordSet records = mapper.readValue(data.toString(), RecordSet.class);
    
    records.userId = "Mr. foobar";
    
    IRecord channel = records.getByChannelName("happy");
    assertTrue(channel instanceof BooleanRecord);
    assertTrue(channel.getValue().equals(true));
    
    String strjson = records.toJson();
    JsonNode json =  mapper.readTree(strjson);
    
    assertTrue(json.get("channels").get("happy").isBoolean());
    assertTrue(!json.get("userId").isNull());
    
  }
  
  @Test
  public void testParseStreamData() throws IOException {
    
    JsonNode data1 = loadData("record1");
    RecordSet records = mapper.readValue(data1.toString(), RecordSet.class);
    
    String strjson = records.toJson();
    JsonNode json = mapper.readTree(strjson);
    
    assertTrue(json.has("channels"));
    assertTrue(json.has("lastUpdate"));
    
  }
  
  @Test
  public void testParseResultSet() throws IOException {
        
    JsonNode resultset = loadData("resultset");
    ResultSet results = mapper.readValue(resultset.toString(), ResultSet.class);
    
    String strjson = results.toJson();
    JsonNode json = mapper.readTree(strjson);
    
    assertTrue(json.size() == results.size());
    
  }
  
  @Test(expected=RaptorComponent.ValidationException.class)
  public void testParseRecordSet() throws IOException {
    
   
    JsonNode resultset = loadData("resultset");
    JsonNode json = resultset.get(0);
    
    serviceObject.parse(jsonServiceObject);
    
    Stream stream = serviceObject.streams.getOrDefault(defaultStreamName, null);
    
    assertNotNull("Stream "+ defaultStreamName +" not found in model", stream);
            
    RecordSet recordSet = new RecordSet(stream, json);
    // should not throw exception
    recordSet.validate();
    
    JsonNode invalidRecord = loadData("recordset_invalid");
    
    recordSet = mapper.convertValue(invalidRecord, RecordSet.class);

    // should not throw exception
    recordSet.validate();
    
    recordSet.setStream(stream);
    
    // should throw an exception now, as there are additional channels
    recordSet.validate();
    
    
    
  }

}
