package com.ringlesoft.visualenv.model;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the CLI Action Definition system
 */
public class CliActionDefinitionTest extends BasePlatformTestCase {

    /**
     * Test basic CLI action creation
     */
    public void testCliActionCreation() {
        CliActionDefinition action = new CliActionDefinition(
            "test-command",  "Test Command",  "test:run",  "This is a test command");
        
        assertEquals("test-command", action.getId());
        assertEquals("Test Command", action.getName());
        assertEquals("test:run", action.getCommand());
        assertEquals("This is a test command", action.getDescription());
    }

}