package cn.mtjsoft.tinypng.plugin;

import cn.mtjsoft.tinypng.plugin.view.AutoWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.tinify.Tinify;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TinyPngAction extends AnAction {

    private AnActionEvent event;
    private Project project;
    /*项目包名*/
    private String packagebase = "";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        event = e;
        project = e.getProject();
        packagebase = readPackageName();
        Tinify.setKey("54vYvMTl6s36kN1q802ln0XqZMl5Q2tg");
        new AutoWindow().showWindow();
    }

    /**
     * 读取包名
     *
     * @return
     */
    private String readPackageName() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/App/src/main/AndroidManifest.xml");
            NodeList dogList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < dogList.getLength(); i++) {
                Node dog = dogList.item(i);
                Element elem = (Element) dog;
                return elem.getAttribute("package");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
