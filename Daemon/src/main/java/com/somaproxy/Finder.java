package com.somaproxy;

import java.io.File;
import java.io.FilenameFilter;

class Finder {

    private String[] filenames;

    void fileFinder(String dirName, String extension) {
        try {
            File dir = new File(dirName);
            // Use filter to get list of files
            filenames = dir.list(new ExtensionFilter(extension));
        }catch (Exception e){
            System.out.println("(FileFinderModule)" + e.getClass().getSimpleName() + " : " + e.getCause());
        }
    }

    String[] getFilenames() {
        return filenames;
    }

    private static class ExtensionFilter implements FilenameFilter {
        private final String extension;
        ExtensionFilter(String ext) {
            extension = ext;
        }
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(extension);
        }
    }

}
