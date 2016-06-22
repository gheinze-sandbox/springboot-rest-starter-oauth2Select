package bike.asi.configmgmt.util.cloudstore;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ParentReference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author gheinze
 */
public class GoogleDrive implements CloudStore {

    // TODO: CloudStore setRootFolder. Given a path, lookup parent id, place files under parent

    private static final List<ParentReference> PARENT_DESTINATION_REFERENCES = Lists.newArrayList(
            new ParentReference().setId("0B4RyV1qO0-W6NUtiejAyUGhCSWc"), // ASI
            new ParentReference().setId("0B4RyV1qO0-W6aUhOR2dCc1o0b2c") // eblox
    );


    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;

    private final Drive drive;


    public GoogleDrive(String clientId, String clientSecret, int authCallbackPort) throws GeneralSecurityException, IOException {

        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        jsonFactory = new JacksonFactory();
        drive = setupGoogleDrive(clientId, clientSecret, authCallbackPort);
    }


    @Override
    public String transferToCloud(MultipartFile multipartFile) throws IOException {

        com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
        body.setTitle(multipartFile.getOriginalFilename());
        body.setParents(PARENT_DESTINATION_REFERENCES);
        body.setDescription("EBlox configuration loaded via ASI Configuration Management");  // TODO: parameter? skip?
        body.setMimeType(multipartFile.getContentType());

        com.google.api.services.drive.model.File googleDriveFile;

        try (InputStream uploadFileStream = multipartFile.getInputStream()) {
            InputStreamContent mediaContent = new InputStreamContent(multipartFile.getContentType(), uploadFileStream);
            googleDriveFile = drive.files().insert(body, mediaContent).execute();
        }

        return "https://drive.google.com/open?id=" + googleDriveFile.getId();

    }



    private Drive setupGoogleDrive(String clientId, String clientSecret, int authCallbackPort) throws IOException {

        // TODO: temp credential store location from config

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientId,
                clientSecret,
                Collections.singleton(DriveScopes.DRIVE_FILE)
        )
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(System.getProperty("user.home"), ".store/drive_sample")))
                .build()
                ;


        LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder()
                .setPort(authCallbackPort)
                .build();


        Credential credential = new AuthorizationCodeInstalledApp(flow, localServerReceiver).authorize("user");

        return new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("ASI Configuration Management")  // TODO: from config?
                .build();

    }


}
