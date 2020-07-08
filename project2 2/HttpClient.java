import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class HttpClient {
    private static String userid = "771";
    private static String xApiKey = "6cfa2149b7382b604e69";
    private static String contentType = "application/x-www-form-urlencoded";
    private static String url = "http://www.notexponential.com/aip2pgaming/api/index.php";

    /**
     * used to send the GET request
     * @param param contains all the parameters of the GET request
     * @return return the response JASON
     */
    static String sendGet(String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;

        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("x-api-key", xApiKey);
            connection.setRequestProperty("userid",userid);
            connection.setRequestProperty("Content-Type", contentType);

            // create connection
            connection.connect();

            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("Something wrong with sending GET request  " + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result.toString();
    }

    /**
     * used to send POST request
     * @param param contains all the parameters of the POST request
     * @return return the response JASON
     */
    static String sendPost(String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // open the connection with the url
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("userid",userid);
            connection.setRequestProperty("x-api-key", xApiKey);
            connection.setRequestProperty("Content-Type", contentType);

            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = new PrintWriter(connection.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("Something wrong with sending POST request  "+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result.toString();

    }
}
