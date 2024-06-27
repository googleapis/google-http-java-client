package com.google.api.client.http.apache.v3;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager.Projects;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager.Projects.Get;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;

public class Main {

  static final String PROJECT_ID = System.getenv("PROJECT_ID");

  public static void main(String[] args) throws IOException {
    ApacheHttpTransport transport = new ApacheHttpTransport();

    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    CloudResourceManager.Builder resourceManagerBuilder =
        new CloudResourceManager.Builder(
            transport, jsonFactory, new HttpCredentialsAdapter(credentials))
            .setApplicationName("Example Java App");
    CloudResourceManager cloudResourceManager = resourceManagerBuilder.build();

    Projects projects = cloudResourceManager.projects();
    Get get = projects.get("projects/"+PROJECT_ID);
    Project project = get.execute();
    System.out.println("Project display name: " + project.getDisplayName());

  }

}
