package com.somaproxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

class Method {

    void run() {
        //Initialize
        String dirName = "C:\\ArkFolder\\test";
        String extension = ".csv";
        String messageParameters;
        String messageFirst = "{\"method\":\"getToken\",\"email\":\"robot@yandex.ru\",\"password\":\"!Selectivity\"}";
        String targetSite = "https://rate.1csmart.com/api/rpc.php";
        String importAddress;
        String exportAddress;
        String newName;
        String cleanName;
        JSONObject jsonObject;
        //Create
        Converter converter = new Converter();
        Finder finder = new Finder();

        finder.fileFinder(dirName, extension);
        String[] base = finder.getFilenames();

        for (String oldName : base) {
            importAddress = dirName + "\\" + oldName;
            cleanName = oldName.replaceAll(".csv", "\0");
            newName = oldName.replaceAll(".csv", ".json");
            exportAddress = dirName + "\\" + newName;
            new File(exportAddress);
            //converter.localConverter(importAddress, exportAddress,messageParameters);
            try {
                converter.writeToServer(messageFirst, targetSite);
                jsonObject = converter.getJsonObject();
                messageParameters = "{\n" +
                        "\"token\":\"" + jsonObject.getJSONObject("result").getJSONObject("token").get("access_token") + "\",\n" +
                        "\"method\":\"table\",\n" +
                        "\"table_name\":\"" + cleanName + "\",\n" +
                        "\"oper\":\"edit\",\n" +
                        "\"data\":";
                converter.webConverter(importAddress, targetSite, messageParameters);
                if (new File(importAddress).delete()) {
                    System.out.println("del");
                } else {
                    System.out.println("nd");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }
}

