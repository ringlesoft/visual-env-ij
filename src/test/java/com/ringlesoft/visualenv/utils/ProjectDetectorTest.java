package com.ringlesoft.visualenv.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.intellij.openapi.util.io.FileUtil.createTempDirectory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ProjectDetector utility class
 */
public class ProjectDetectorTest extends BasePlatformTestCase {
    private File tempDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tempDir = createTempDirectory("visualEnv", "");
    }

    @Override
    protected void tearDown() throws Exception {
        if (tempDir != null && tempDir.exists()) {
            deleteRecursively(tempDir);
        }
        super.tearDown();
    }

    public void testCheckComposerForLaravel() throws IOException {
        File laravelProjectDir = new File(tempDir, "laravel-project");
        laravelProjectDir.mkdir();

        // Create artisan file
        File artisanFile = new File(laravelProjectDir, "artisan");
        Files.write(artisanFile.toPath(), "<?php // Laravel artisan file".getBytes(StandardCharsets.UTF_8));
        assertTrue("Should detect Laravel project structure", true); // TODO Implement
    }

    public void testHasLaravelDirectoryStructure() {
        File laravelProjectDir = new File(tempDir, "laravel-project");
        laravelProjectDir.mkdir();

        // Create artisan file
        File artisanFile = new File(laravelProjectDir, "artisan");
        assertTrue("Should detect Laravel project structure", true); // TODO Implement
    }

    public void testHasDjangoDirectoryStructure() {
        File laravelProjectDir = new File(tempDir, "django-project");
        laravelProjectDir.mkdir();

        // Create artisan file
        File artisanFile = new File(laravelProjectDir, "models.py");
        assertTrue("Should detect Django project structure", true); // TODO Implement
    }

//    // TODO Configure Mockito to be able to run the below tests
//    /**
//     * Test Laravel project detection
//     */
//    public void testIsLaravelProject() throws IOException {
//        // Create Laravel project structure
//        File laravelProjectDir = new File(tempDir, "laravel-project");
//        laravelProjectDir.mkdir();
//
//        // Create artisan file
//        File artisanFile = new File(laravelProjectDir, "artisan");
//        Files.write(artisanFile.toPath(), "<?php // Laravel artisan file".getBytes(StandardCharsets.UTF_8));
//
//        // Create composer.json with Laravel dependency
//        File composerFile = new File(laravelProjectDir, "composer.json");
//        String composerJson = "{\"name\": \"laravel/test\", \"require\": {\"laravel/framework\": \"^8.0\"}}";
//        Files.write(composerFile.toPath(), composerJson.getBytes(StandardCharsets.UTF_8));
//
//        // Refresh VFS
//        VirtualFile laravelVF = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(laravelProjectDir);
//
//        Project project = createMockProject(laravelProjectDir.getPath());
//
//
//        // Test detection
//        if (laravelVF != null) {
//            boolean isLaravel = ProjectDetector.isLaravelProject(project);
//            assertTrue("Laravel project should be detected correctly", isLaravel);
//        }
//
//        // Test negative case
//        File nonLaravelDir = new File(tempDir, "non-laravel");
//        nonLaravelDir.mkdir();
//        VirtualFile nonLaravelVF = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(nonLaravelDir);
//
//        if (nonLaravelVF != null) {
//            boolean isLaravel = ProjectDetector.isLaravelProject(project);
//            assertFalse("Non-Laravel project should not be detected as Laravel", isLaravel);
//        }
//    }
//
//
//    /**
//     * Test Node.js project detection
//     */
//    public void testIsNodeJsProject() throws IOException {
//        // Create Node.js project structure
//        File nodeProjectDir = new File(tempDir, "node-project");
//        nodeProjectDir.mkdir();
//
//        // Create package.json
//        File packageFile = new File(nodeProjectDir, "package.json");
//        String packageJson = "{\"name\": \"node-app\", \"version\": \"1.0.0\"}";
//        Files.write(packageFile.toPath(), packageJson.getBytes(StandardCharsets.UTF_8));
//
//        // Refresh VFS
//        VirtualFile nodeVF = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(nodeProjectDir);
//
//        // Test detection
//        if (nodeVF != null) {
//            boolean isNodeJs = ProjectDetector.isNodeJsProject(nodeVF);
//            assertTrue("Node.js project should be detected correctly", isNodeJs);
//        }
//
//        // Test negative case
//        File nonNodeDir = new File(tempDir, "non-node");
//        nonNodeDir.mkdir();
//        VirtualFile nonNodeVF = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(nonNodeDir);
//
//        if (nonNodeVF != null) {
//            boolean isNodeJs = ProjectDetector.isNodeJsProject(nonNodeVF);
//            assertFalse("Non-Node project should not be detected as Node.js", isNodeJs);
//        }
//    }

    /**
     * Test Django project detection
     */
//    public void testIsDjangoProject() throws IOException {
//        // Create Django project structure
//        File djangoProjectDir = new File(tempDir, "django-project");
//        djangoProjectDir.mkdir();
//
//        // Create manage.py
//        File manageFile = new File(djangoProjectDir, "manage.py");
//        Files.write(manageFile.toPath(), "#!/usr/bin/env python\n# Django manage.py".getBytes(StandardCharsets.UTF_8));
//
//        // Refresh VFS
//        VirtualFile djangoVF = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(djangoProjectDir);
//
//        // Test detection
//        if (djangoVF != null) {
//            boolean isDjango = ProjectDetector.isDjangoProject(djangoVF);
//            assertTrue("Django project should be detected correctly", isDjango);
//        }
//
//        // Test negative case
//        File nonDjangoDir = new File(tempDir, "non-django");
//        nonDjangoDir.mkdir();
//        VirtualFile nonDjangoVF = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(nonDjangoDir);
//
//        if (nonDjangoVF != null) {
//            boolean isDjango = ProjectDetector.isDjangoProject(nonDjangoVF);
//            assertFalse("Non-Django project should not be detected as Django", isDjango);
//        }
//    }


    private Project createMockProject(String path) {
        Project mockProject = mock(Project.class);
        when(mockProject.getBasePath()).thenReturn(path);
        return mockProject;
    }

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