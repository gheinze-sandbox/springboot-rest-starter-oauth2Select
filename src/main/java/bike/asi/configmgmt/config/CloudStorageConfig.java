package bike.asi.configmgmt.config;

import bike.asi.configmgmt.util.cloudstore.CloudStore;
import bike.asi.configmgmt.util.cloudstore.GoogleDrive;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author gheinze
 */
@Configuration
public class CloudStorageConfig {

    @Value("${google.drive.clientId}") private String googleDriveClientId;
    @Value("${google.drive.clientSecret}") private String googleDriveClientSecret;
    @Value("${google.drive.callBackPort}") private int googleDriveCallbackPort;


    @Bean
    public CloudStore googleDrive() throws GeneralSecurityException, IOException {
        return new GoogleDrive(
                googleDriveClientId,
                googleDriveClientSecret,
                googleDriveCallbackPort
        );
    }

}
