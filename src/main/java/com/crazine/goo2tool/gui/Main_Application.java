package com.crazine.goo2tool.gui;

import com.crazine.goo2tool.addinFile.AddinFileLoader;
import com.crazine.goo2tool.addinFile.Goo2mod;
import com.crazine.goo2tool.properties.Addin;
import com.crazine.goo2tool.properties.Properties;
import com.crazine.goo2tool.properties.PropertiesLoader;
import com.crazine.goo2tool.saveFile.SaveFileLoader;
import com.crazine.goo2tool.saveFile.WOG2SaveData;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public class Main_Application extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        PropertiesLoader.init();

        FX_Scene.buildScene(stage);
        FX_Menu.buildMenuBar(stage);
        FX_Profile.buildProfileView(stage);
        FX_Mods.buildModView(stage);
        FX_Options.buildOptionsView(stage);

        stage.setWidth(960);
        stage.setHeight(540);

        Scene scene = FX_Scene.getScene();
        stage.setTitle("World of Goo 2 Tool");
        stage.setScene(scene);
        stage.show();

        File toSaveFile = new File("C:/Users/Crazydiamonde/AppData/Local/2DBoy/WorldOfGoo2/wog2_1.dat");

        try {
            WOG2SaveData[] data = SaveFileLoader.readSaveFile(toSaveFile);
            for (WOG2SaveData.WOG2SaveFileLevel level : data[0].getSaveFile().getIslands()[0].getLevels())
                FX_Profile.getLevelTableView().getItems().add(level);

            Goo2mod goo2mod = AddinFileLoader.loadGoo2mod(new File(System.getenv("APPDATA") + "/Goo2Tool/addins/TestGoomod.goo2mod"));
            Addin addin2 = new Addin();
            addin2.setName(goo2mod.getId());
            addin2.setLoaded(false);
            if (Arrays.stream(PropertiesLoader.getProperties().getAddins()).noneMatch(addin -> addin.getName().equals(addin2.getName()))) {
                Addin[] addins2 = new Addin[PropertiesLoader.getProperties().getAddins().length + 1];
                System.arraycopy(PropertiesLoader.getProperties().getAddins(), 0, addins2, 0, addins2.length - 1);
                addins2[addins2.length - 1] = addin2;
                PropertiesLoader.getProperties().setAddins(addins2);
            }
            FX_Mods.getModTableView().getItems().add(goo2mod);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
