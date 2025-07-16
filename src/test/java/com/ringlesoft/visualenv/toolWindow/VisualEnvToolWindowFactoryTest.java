package com.ringlesoft.visualenv.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.lang.reflect.Field;

/**
 * Tests for the VisualEnvToolWindowFactory class
 */
public class VisualEnvToolWindowFactoryTest extends BasePlatformTestCase {
    private VisualEnvToolWindowFactory toolWindowFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        toolWindowFactory = new VisualEnvToolWindowFactory();
    }

    /**
     * Test basic factory creation
     */
    public void testFactoryCreation() {
        assertNotNull("ToolWindowFactory should be created successfully", toolWindowFactory);
    }

    /**
     * Test tool window availability
     */
    public void testToolWindowAvailability() {
        Project project = getProject();
        boolean shouldBeAvailable = toolWindowFactory.shouldBeAvailable(project);
        assertTrue("Tool window should be available for all projects", shouldBeAvailable);
    }

    /**
     * Test UI component initialization
     * This test checks if the main UI fields are initialized correctly
     */
    public void testComponentInitialization() {
        try {
            // Use reflection to check if key fields are initialized
            Field contentPanelField = VisualEnvToolWindowFactory.class.getDeclaredField("contentPanel");
            Field tabbedPaneField = VisualEnvToolWindowFactory.class.getDeclaredField("tabbedPane");
            Field mainPanelField = VisualEnvToolWindowFactory.class.getDeclaredField("mainPanel");
            Field controlPanelField = VisualEnvToolWindowFactory.class.getDeclaredField("controlPanel");
            
            contentPanelField.setAccessible(true);
            tabbedPaneField.setAccessible(true);
            mainPanelField.setAccessible(true);
            controlPanelField.setAccessible(true);
            
            // Fields should exist (this just tests that the fields exist in the class)
            assertNotNull("contentPanel field should exist", contentPanelField);
            assertNotNull("tabbedPane field should exist", tabbedPaneField);
            assertNotNull("mainPanel field should exist", mainPanelField);
            assertNotNull("controlPanel field should exist", controlPanelField);
            
            // Note: We can't test the actual initialization without a proper ToolWindow,
            // which is difficult to create in a test environment
        } catch (NoSuchFieldException e) {
            fail("Required UI component field not found: " + e.getMessage());
        }
    }

    /**
     * Test creating the control panel
     */
    public void testCreateControlPanel() {
        try {
            // Use reflection to access the private createControlPanel method
            java.lang.reflect.Method createControlPanelMethod = 
                VisualEnvToolWindowFactory.class.getDeclaredMethod("createControlPanel");
            createControlPanelMethod.setAccessible(true);
            
            // Method should exist (just checking it's defined)
            assertNotNull("createControlPanel method should exist", createControlPanelMethod);
            
            // Note: We can't actually invoke this method in isolation as it depends on
            // envService being initialized, which requires a real ToolWindow context
        } catch (NoSuchMethodException e) {
            fail("createControlPanel method not found: " + e.getMessage());
        }
    }

    /**
     * Test creating the CLI actions panel
     */
    public void testCreateCliActionsPanel() {
        try {
            // Use reflection to access the private createCliActionsPanel method
            java.lang.reflect.Method createCliActionsPanelMethod = 
                VisualEnvToolWindowFactory.class.getDeclaredMethod("createCliActionsPanel");
            createCliActionsPanelMethod.setAccessible(true);
            
            // Method should exist
            assertNotNull("createCliActionsPanel method should exist", createCliActionsPanelMethod);
            
            // Note: Can't invoke without proper context
        } catch (NoSuchMethodException e) {
            fail("createCliActionsPanel method not found: " + e.getMessage());
        }
    }

    /**
     * Test UI update method
     */
    public void testUpdateUI() {
        try {
            // Use reflection to access the private updateUI method
            java.lang.reflect.Method updateUIMethod = 
                VisualEnvToolWindowFactory.class.getDeclaredMethod("updateUI");
            updateUIMethod.setAccessible(true);
            
            // Method should exist
            assertNotNull("updateUI method should exist", updateUIMethod);
            
            // Note: Can't invoke without proper context
        } catch (NoSuchMethodException e) {
            fail("updateUI method not found: " + e.getMessage());
        }
    }

    /**
     * Test that the factory can be closed without errors
     */
    public void testClose() throws Exception {
        // Just make sure the close method doesn't throw any exceptions
        toolWindowFactory.close();
    }
}