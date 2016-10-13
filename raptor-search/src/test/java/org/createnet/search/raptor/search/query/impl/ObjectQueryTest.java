/*
 * Copyright 2016 Luca Capra <lcapra@create-net.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.search.raptor.search.query.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.createnet.raptor.search.raptor.search.query.Query;
import org.elasticsearch.index.query.QueryBuilder;
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
public class ObjectQueryTest {

  String content;
  ObjectMapper mapper = new ObjectMapper();

  public ObjectQueryTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    Path path = Paths.get(classLoader.getResource("object-query.json").getPath());
    content = new String(Files.readAllBytes(path));
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of validate method, of class ObjectQuery.
   */
//  @Test
//  public void testValidate() throws Query.QueryException {
//    System.out.println("validate");
//    ObjectQuery instance = mapper.readValue(content, ObjectQuery.class);
//    instance.validate();
//  }

//  @Test
//  public void testBuildQuery_freeText() throws IOException {
//    System.out.println("buildQuery with free text");
//    ObjectQuery instance = mapper.readValue(content, ObjectQuery.class);
//    QueryBuilder result = instance.buildQuery();
//    System.out.println("org.createnet.search.raptor.search.query.impl.ObjectQueryTest.testBuildQuery() "  + result.toString());
//  }
  
//  @Test
//  public void testBuildQuery_parts() throws IOException {
//    System.out.println("buildQuery with parts");
//    ObjectQuery instance = mapper.readValue(content, ObjectQuery.class);
//    QueryBuilder result = instance.buildQuery();
//    System.out.println("org.createnet.search.raptor.search.query.impl.ObjectQueryTest.testBuildQuery() "  + result.toString());
//  }

  /**
   * Test of format method, of class ObjectQuery.
   */
//  @Test
//  public void testFormat() throws Exception {
//    System.out.println("format");
//    ObjectQuery instance = new ObjectQuery();
//    String expResult = "";
//    String result = instance.format();
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }

}
