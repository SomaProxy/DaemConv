package com.somaproxy;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class Converter {

    private ArrayList<String> linesRead = new ArrayList<>();
    private JSONObject jsonObject;
    JSONObject getJsonObject() {
        return jsonObject;
    }
    @NotNull
    private String convertToJSON(String messageCondition) {
        StringBuilder content;
        String regSplit = ";";
        String[] headers = linesRead.get(0).split(regSplit);
        int end = linesRead.size();
        content = new StringBuilder();
        content.append(messageCondition);
        content.append("[\n");
        for (int i = 1; i < end; i++) {
            String[] currentLineContents = linesRead.get(i).split(regSplit);
            content.append("{\n");
            for (int j = 0; j < headers.length; j++) {
                content.append("\"").append(headers[j]).append("\"").append(":").append("\"").append(currentLineContents[j]).append("\"");
                if (j != currentLineContents.length - 1) {
                    content.append(",\n");
                }
            }
            content.append("\n}");
            if (i != end - 1) {
                content.append(",\n");
            }
        }
        content.append("\n]\n");
        content.append("}\n");
        return content.toString();
    }
    void writeToServer(String jsonString, String exportName) {
        try {
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.clearProperty("javax.net.ssl.trustStore");
            // URL and parameters for the connection, This particularly returns the information passed
            URL url = new URL(exportName);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches (false);
            System.out.println(httpURLConnection.getOutputStream());
            // Writes the JSON parsed as string to the connection
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(jsonString.getBytes());
            System.out.println(jsonString);
            int responseCode = httpURLConnection.getResponseCode();
            // Creates a reader buffer
            BufferedReader bufferedReader;
            if (responseCode > 199 && responseCode < 300) {
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
            }
            // To receive the response
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
            // Prints the response
            jsonObject = new JSONObject(content.toString());
            System.out.println(content.toString());
        } catch (Exception e) {
            System.out.println("(ServerWriterModule)" + e.getClass().getSimpleName() + " : " + e.getCause());
        }
    }
    private void readFileCSV(String aFileName) {
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(aFileName));
            while ((line = bufferedReader.readLine()) != null) {
                line += "\0"; //<--
                linesRead.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }
    private void writeToFile(String jsonString, String exportName) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(exportName));
            out.print(jsonString);
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }
    void webConverter(String addressImportCSV, String addressExportJSON, String messageParameters) {
        readFileCSV(addressImportCSV);
        writeToServer(convertToJSON(messageParameters), addressExportJSON);
        System.out.println("Conversion completed. File pulled to " + addressExportJSON);
        linesRead.clear();
    }
    void localConverter(String addressImportCSV, String addressExportJSON, String messageParameters) {
        readFileCSV(addressImportCSV);
        writeToFile(convertToJSON(messageParameters), addressExportJSON);
        System.out.println("Conversion completed. File saved as " + addressExportJSON);
        linesRead.clear();
    }
}
