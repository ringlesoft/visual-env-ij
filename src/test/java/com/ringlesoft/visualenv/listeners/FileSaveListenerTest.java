package com.ringlesoft.visualenv.listeners;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.util.messages.MessageBusConnection;
import com.ringlesoft.visualenv.services.EnvVariableService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.intellij.openapi.util.io.FileUtil.createTempDirectory;

/**
 * Tests for the FileSaveListener class
 */
public class FileSaveListenerTest extends BasePlatformTestCase {
    private FileSaveListener fileSaveListener;
    private Project project;
    private EnvVariableService envVariableService;
    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = getProject();
        fileSaveListener = new FileSaveListener(project, envVariableService);
        tempDir = createTempDirectory("VisualEnv", "");
    }

    @Override
    protected void tearDown() throws Exception {
        if (tempDir != null && tempDir.exists()) {
            deleteRecursively(tempDir);
        }
        super.tearDown();
    }

    /**
     * Test that the listener can be created without errors
     */
    public void testListenerCreation() {
        assertNotNull("FileSaveListener should be created successfully", fileSaveListener);
    }


//    /**
//     * Test environment file detection
//     */
//    public void testIsEnvFile() {
//        try {
//            // Create test env files
//            File envFile = new File(tempDir, ".env");
//            File envLocalFile = new File(tempDir, ".env.local");
//            File nonEnvFile = new File(tempDir, "regular.txt");
//
//            envFile.createNewFile();
//            envLocalFile.createNewFile();
//            nonEnvFile.createNewFile();
//
//            VirtualFile envVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(envFile);
//            VirtualFile envLocalVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(envLocalFile);
//            VirtualFile nonEnvVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(nonEnvFile);
//
//            // Use reflection to access the private isEnvFile method
//            Method isEnvFileMethod = FileSaveListener.class.getDeclaredMethod("isEnvFile", VirtualFile.class);
//            isEnvFileMethod.setAccessible(true);
//
//            // Test with different files
//            boolean isEnvFileResult = (Boolean) isEnvFileMethod.invoke(fileSaveListener, envVirtualFile);
//            boolean isEnvLocalFileResult = (Boolean) isEnvFileMethod.invoke(fileSaveListener, envLocalVirtualFile);
//            boolean isNonEnvFileResult = (Boolean) isEnvFileMethod.invoke(fileSaveListener, nonEnvVirtualFile);
//
//            assertTrue(".env file should be detected as env file", isEnvFileResult);
//            assertTrue(".env.local file should be detected as env file", isEnvLocalFileResult);
//            assertFalse("regular.txt should not be detected as env file", isNonEnvFileResult);
//        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException | IOException e) {
//            fail("Exception during test: " + e.getMessage());
//        }
//    }

//    /**
//     * Test environment file parsing on save
//     */
//    public void testHandleEnvFileSave() {
//        try {
//            // Create a test .env file
//            File envFile = new File(tempDir, ".env");
//            Files.write(envFile.toPath(), (
//                "APP_NAME=Test App\n" +
//                "DB_HOST=localhost\n"
//            ).getBytes(StandardCharsets.UTF_8));
//
//            VirtualFile virtualEnvFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(envFile);
//
//            // Get the EnvVariableService
//            EnvVariableService envService = project.getService(EnvVariableService.class);
//            assertNotNull("EnvVariableService should be available", envService);
//
//            // Try to trigger file saved event (this is simplified since we can't easily trigger real events)
//            try {
//                Method handleEnvFileSaveMethod = FileSaveListener.class.getDeclaredMethod("handleEnvFileSave", VirtualFile.class);
//                handleEnvFileSaveMethod.setAccessible(true);
//                handleEnvFileSaveMethod.invoke(fileSaveListener, virtualEnvFile);
//            } catch (NoSuchMethodException e) {
//                // Method might have different signature, try alternative approach
//                fileSaveListener.after(createMockVFileEvent(virtualEnvFile));
//            }
//
//            // Test is more of a sanity check that the method exists and doesn't throw exceptions
//            // In a real environment, we'd verify that the service parsed the file
//
//        } catch (Exception e) {
//            fail("Exception during test: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Create a mock VFileEvent for testing
//     */
//    private VFileContentChangeEvent createMockVFileEvent(VirtualFile file) {
//        return new VFileContentChangeEvent(this, file, file.getModificationStamp() - 1, file.getModificationStamp(), false);
//    }
//
    /**
     * Helper to delete directories recursively
     */
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}