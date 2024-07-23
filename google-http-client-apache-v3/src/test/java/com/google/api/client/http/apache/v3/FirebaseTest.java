package com.google.api.client.http.apache.v3;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FirebaseTest {
    public static FirebaseApp setup_admin_apache_http2() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/cert.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setHttpTransport(new ApacheHttpTransport())
                .build();

        return FirebaseApp.initializeApp(options);
    }

    public static List<Message> get_messages(int message_count) {
        List<Message> messages = new ArrayList<>(message_count);
        for (int i = 0; i < message_count; i++) {
            Message message = Message.builder()
                    .setTopic(String.format("foo-bar-%d", i % 10))
                    .build();
            messages.add(message);
        }
        return messages;
    }

    public static void benchmark_send_each(List<Message> messages, int numRequests, FirebaseApp app) throws FileNotFoundException, IOException, FirebaseMessagingException {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numRequests; i++) {
            BatchResponse response = FirebaseMessaging.getInstance(app).sendEach(messages, true);
            System.out.println("Dry Run Response: " + response.getSuccessCount());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numRequests;

        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time per request: " + averageTime + " ms");
    }


    public static void benchmark_send_each_async(List<Message> messages, int numRequests, FirebaseApp app) throws FileNotFoundException, IOException, FirebaseMessagingException {
        long startTime = System.currentTimeMillis();
        List<ApiFuture<BatchResponse>> responseFutures = new ArrayList<>();

        // Make request futures
        for (int i = 0; i < numRequests; i++) {
            responseFutures.add(FirebaseMessaging.getInstance(app).sendEachAsync(messages, true));
        }

        // Resolve All
        try {
            List<BatchResponse> responses = ApiFutures.allAsList(responseFutures).get();
            for (BatchResponse batchResponse : responses) {
                System.out.println("Dry Run Response: " + batchResponse.getSuccessCount());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new IOException("Error making request", e);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numRequests;

        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time per request: " + averageTime + " ms");
    }

    @SuppressWarnings("deprecation")
    public static void benchmark_send_all(List<Message> messages, int numRequests, FirebaseApp app) throws FileNotFoundException, IOException, FirebaseMessagingException {
        System.out.println("\nsendAll()");
        for (int i = 0; i < numRequests; i++) {
            FirebaseMessaging.getInstance().sendEach(messages, true);
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numRequests; i++) {
            BatchResponse response = FirebaseMessaging.getInstance(app).sendAll(messages, true);
            System.out.println("Dry Run Response: " + response.getSuccessCount());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numRequests;

        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Average time per request: " + averageTime + " ms");

        // app.delete();
    }

    @Test
    public void testFireBase() throws FirebaseMessagingException, IOException, InterruptedException {


        List<Message> messages = get_messages(99);
        int numRequests = 50; // Number of time to loop
        FirebaseApp app;

        System.out.println("Start");
        app = setup_admin_apache_http2();
        benchmark_send_each(messages, numRequests, app);
        benchmark_send_each_async(messages, numRequests, app);
        benchmark_send_each(messages, numRequests, app);

         System.out.println("Sleep");
         TimeUnit.SECONDS.sleep(5);
         System.out.println("Awake");

        app.delete();

    }

}
