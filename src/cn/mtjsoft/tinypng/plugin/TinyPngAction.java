package cn.mtjsoft.tinypng.plugin;

import cn.mtjsoft.tinypng.plugin.view.AutoWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.tinify.Tinify;
import org.jetbrains.annotations.NotNull;

public class TinyPngAction extends AnAction {

    private AnActionEvent event;
    private Project project;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        event = e;
        project = e.getProject();
        Tinify.setKey("54vYvMTl6s36kN1q802ln0XqZMl5Q2tg");
        new AutoWindow().showWindow();
    }
}
