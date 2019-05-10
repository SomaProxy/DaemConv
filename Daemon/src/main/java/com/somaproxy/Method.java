package com.somaproxy;

import java.io.File;

class Method {

    void run() {
        //Initialize parameters
        String dirName = "C:\\ArkFolder\\test";
        String extension = ".csv";
        String importAddress;
        String exportAddress;
        String newName;
        //Create objects
        Converter converter = new Converter();
        Finder finder = new Finder();

        finder.fileFinder(dirName, extension);
        String[] base = finder.getFilenames();

        for (String s : base) {
            importAddress = dirName + "\\" + s;
            newName = s.replaceAll(".csv", ".json");
            exportAddress = dirName + "\\" + newName;
            new File(exportAddress);


            converter.localConverter(importAddress, exportAddress);
            //converter.webConverter(importAddress,exportAddress);
        }
    }
}

