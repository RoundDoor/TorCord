package dev.rounddoor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;


public class Main {
    public static void main(String[] args) {

        apiTest(1);

    }



/**
 * This method is used to test the API. It sends a GET request to the API and prints the response.
 *
 * @param msgID This is the message ID that will be appended to the API URL.
 */
public static void apiTest(int msgID){
    // Create an OkHttpClient to send the request
    OkHttpClient client = new OkHttpClient();

    // Build the request with the specified URL
    Request request = new Request.Builder()
            .url("http://127.0.0.1:8000/msg/" + msgID)
            .build();

    try {
        // Execute the request and get the response
        Response response = client.newCall(request).execute();

        // Parse the response body to a JSONObject
        assert response.body() != null;
        JSONObject jsonObject = new JSONObject(response.body().string());

        // Print the JSONObject
        System.out.println(jsonObject.toString());
    } catch (Exception e) {
        // Print the stack trace for any exceptions
        e.printStackTrace();
    }
}


}

