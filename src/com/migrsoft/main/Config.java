package com.migrsoft.main;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class Config {

    private static Config instance = null;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private String tesserOcrDataPath = "";

    public String getTesserOcrDataPath() {
        return tesserOcrDataPath;
    }

    public void setTesserOcrDataPath(String tesserOcrDataPath) {
        this.tesserOcrDataPath = tesserOcrDataPath;
    }

    private String deepLApiKey = "";

    public String getDeepLApiKey() {
        return deepLApiKey;
    }

    public void setDeepLApiKey(String deepLApiKey) {
        this.deepLApiKey = deepLApiKey;
    }

    private String getPath() {
        String home = System.getProperty("user.home");
        home += "/.comic_crop.cfg";
        return home;
    }

    public void load() {
        String name = getPath();
        File file = new File(name);
        System.out.println("Load: " + name);
        if (file.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Config cfg = mapper.readValue(file, Config.class);
                setTesserOcrDataPath(cfg.getTesserOcrDataPath());
                setDeepLApiKey(cfg.getDeepLApiKey());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            save(name);
        }
    }

    private void save(String name) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(name), getInstance());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
