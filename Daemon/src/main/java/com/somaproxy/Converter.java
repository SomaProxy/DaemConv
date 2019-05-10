package com.somaproxy;

class Converter {

    static {
        disableSSL();
    }

    private java.util.ArrayList<String> linesRead = new java.util.ArrayList<>();

    private static void disableSSL() {
        try {
            // Create a trust manager that does not validate certificate chains
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }};
            // Ignore differences between given hostname and certificate hostname
            javax.net.ssl.HostnameVerifier allHostsValid = (hostname, session) -> true;
            // Install the all-trusting trust manager
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // Install the all-trusting host verifier
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (java.security.NoSuchAlgorithmException | java.security.KeyManagementException e) {
            System.out.println("(DisableSSLModule)" + e.getClass().getSimpleName() + " : " + e.getCause());
        }
    }

    private String convertToJSON() {
        StringBuilder content;
        String regSplit = ";";
        String messageCondition = "{\n" +
                "    \"token\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnZXRyYXRlIiwic3ViIjoicm9ib3RAeWFuZGV4LnJ1IiwiaWF0IjoxNTUwMTUyNzYxLCJqdGkiOiJjMGZmNTQwOTQ5YTBmZjJkNDBkZWExMTEzNzdmYjE0ZjMwZDFiM2Y1IiwiZXhwIjoxNTUwMTU5OTYxfQ.a7I7I_EOn_iEtAERiyXI89ryXIcEvq1Jqp1duFVUXvY\",\n" +
                "    \"method\": \"table\",\n" +
                "    \"table_name\": \"rate\",\n" +
                "    \"oper\": \"edit\",\n" +
                "    \"data\": ";
        String[] headers = linesRead.get(0).split(regSplit);
        int end = linesRead.size();
        content = new StringBuilder();
        content.append(messageCondition);
        content.append("[\n");
        for (int i = 1; i < end; i++) {
            String[] currentLineContents = linesRead.get(i).split(regSplit);
            content.append("{\n");
            for (int j = 0; j < headers.length; j++) {
                content.append("\"").append(headers[j]).append("\"").append(": ").append("\"").append(currentLineContents[j]).append("\"");
                if (j != currentLineContents.length - 1) {
                    content.append(", \n");
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

    private void writeToServer(String jsonString, String exportName) {
        try {
            // Change security provider (if ancient sdk don't support TLSv1.2)
            //java.security.Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 2);
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.clearProperty("javax.net.ssl.trustStore");

            // URL and parameters for the connection, This particularly returns the information passed
            java.net.URL url = new java.net.URL(exportName);

            java.net.HttpURLConnection httpURLConnection = (java.net.HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            System.out.println(httpURLConnection.getOutputStream());
            // Writes the JSON parsed as string to the connection
            java.io.DataOutputStream dataOutputStream = new java.io.DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(jsonString.getBytes());
            int responseCode = httpURLConnection.getResponseCode();
            // Creates a reader buffer
            java.io.BufferedReader bufferedReader;
            if (responseCode > 199 && responseCode < 300) {
                bufferedReader = new java.io.BufferedReader(new java.io.InputStreamReader(httpURLConnection.getInputStream()));
            } else {
                bufferedReader = new java.io.BufferedReader(new java.io.InputStreamReader(httpURLConnection.getErrorStream()));
            }
            // To receive the response
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
            // Prints the response
            System.out.println(content.toString());
        } catch (Exception e) {
            System.out.println("(ServerWriterModule)" + e.getClass().getSimpleName() + " : " + e.getCause());
        }
    }

    private void readFileCSV(String aFileName) {
        String line;
        try {
            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(new java.io.FileReader(aFileName));
            while ((line = bufferedReader.readLine()) != null) {
                line += " "; //<--
                linesRead.add(line);
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("FileNotFoundException");
        } catch (java.io.IOException e) {
            System.out.println("IOException");
        }
    }

    private void writeToFile(String jsonString, String exportName) {
        try {
            java.io.PrintStream out = new java.io.PrintStream(new java.io.FileOutputStream(exportName));
            out.print(jsonString);
        } catch (java.io.IOException e) {
            System.out.println("IOException");
        }
    }

    void localConverter(String addressImportCSV, String addressExportJSON) {
        readFileCSV(addressImportCSV);
        writeToFile(convertToJSON(), addressExportJSON);
        System.out.println("Conversion completed. File saved as " + addressExportJSON);
        linesRead.clear();
    }

    void webConverter(String addressImportCSV, String addressExportJSON) {
        readFileCSV(addressImportCSV);
        writeToServer(convertToJSON(), addressExportJSON);
        System.out.println("Conversion completed. File pulled to " + addressExportJSON);
        linesRead.clear();
    }
}
