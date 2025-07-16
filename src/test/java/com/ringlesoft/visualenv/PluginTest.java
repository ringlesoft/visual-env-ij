package com.ringlesoft.visualenv;

import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.ringlesoft.visualenv.model.EnvVariable;

/**
 * Basic sanity tests for the Visual Env plugin
 */
@TestDataPath("$CONTENT_ROOT/src/test/testData")
public class PluginTest extends BasePlatformTestCase {

    /**
     * Test the EnvVariable model class
     */
    public void testEnvVariableModel() {
        // Test basic variable creation
        EnvVariable variable = new EnvVariable("DB_HOST", "localhost", "/test/path", false, "database");
        assertEquals("DB_HOST", variable.getName());
        assertEquals("localhost", variable.getValue());
        assertEquals("database", variable.getGroup());
        assertFalse(variable.isSecret());
        
        // Test secret variable
        EnvVariable secretVar = new EnvVariable("API_KEY", "secret123", "/test/path", true, "api");
        assertTrue(secretVar.isSecret());
    }
    
    /**
     * Test environment variable detection in various formats
     */
    public void testEnvVariableFormats() {
        // Test with quotes
        EnvVariable quotedVar = new EnvVariable("APP_NAME", "\"My App\"", "/test/path", false, "app");
        assertEquals("\"My App\"", quotedVar.getValue());
        
        // Test with special characters
        EnvVariable specialCharsVar = new EnvVariable("APP_URL", "http://localhost:8000", "/test/path", false, "app");
        assertEquals("http://localhost:8000", specialCharsVar.getValue());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/rename";
    }
}
