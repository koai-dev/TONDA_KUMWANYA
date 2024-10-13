package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebServiceRest {


    public static String sentFCMNotification(String jsonString) {

        String output = "";
        HttpURLConnection connection = null;
        try {
            URL url = new URL(ConstantData.FCM_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", ConstantData.FCM_SERVER_KEY);
            connection.setUseCaches(false);
            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(60 * 1000);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

            wr.writeBytes(jsonString.toString());

            wr.flush();
            wr.close();

            int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                output = convertStreamToString(connection.getInputStream());
            } else
                output = connection.getResponseMessage();

        } catch (Exception e) {
            output = "Error : " + e.getMessage();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return output;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}





