/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.createnet.servioticy.configmanager.configuration;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author l
 */
public class ConfigManagerTest {
    
    public ConfigManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of load method, of class ConfigManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testLoad() throws Exception {
        
        String path = (new File("")).getAbsoluteFile().getAbsolutePath();
        
        System.out.println("load " + path);
        
        String[] paths = new String[] { path + "/src/test/resources/config.yml" };

        ConfigManager instance = new ConfigManager();
        TestClass result = (TestClass) instance.load(TestClass.class, paths);
        
        assertEquals(result.property1, "test1");
        assertEquals(result.property2, "test2");
    }
    
}
