package cn.mtjsoft.tinypng.plugin;

import cn.mtjsoft.tinypng.plugin.utils.CacheUtils;
import cn.mtjsoft.tinypng.plugin.view.AutoWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TinyPngAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        String pathRight = "";
        if (project != null) {
            try {
                String path = Objects.requireNonNull(project.getProjectFile()).getParent().getParent().getPath();
                CacheUtils.INSTANCE.saveProjectFilePath(path);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        try {
            VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (file != null && file.exists()) {
                pathRight = file.getPath();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        new AutoWindow().showWindow(pathRight);
    }
}
