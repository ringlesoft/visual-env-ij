package com.ringlesoft.visualenv.startup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Startup activity for Visual Env plugin.
 */
public class ProjectStartupActivity implements ProjectActivity {
    private static final Logger LOG = Logger.getInstance(ProjectStartupActivity.class);

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        LOG.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
        return Unit.INSTANCE;
    }
}
