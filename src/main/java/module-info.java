module com.crazine.goo2tool {

    requires transitive javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires transitive com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires java.xml;

    exports com.crazine.goo2tool;
    exports com.crazine.goo2tool.gui;
    exports com.crazine.goo2tool.gamefiles.savefile;
    exports com.crazine.goo2tool.addinFile;
    exports com.crazine.goo2tool.properties;
    exports com.crazine.goo2tool.functional;
    exports com.crazine.goo2tool.gamefiles;
    exports com.crazine.goo2tool.gamefiles.filetable;
    exports com.crazine.goo2tool.gamefiles.islands;
    exports com.crazine.goo2tool.gamefiles.resrc;
    exports com.crazine.goo2tool.gamefiles.translation;

}