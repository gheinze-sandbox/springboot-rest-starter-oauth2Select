package bike.asi.configmgmt.util.cloudstore;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author gheinze
 */
public interface CloudStore {

    /**
     * Transfer the file to cloud storage.
     *
     * @param multipartFile
     * @return The cloud store reference of the file just upload.
     * @throws java.io.IOException
     */
    String transferToCloud(MultipartFile multipartFile) throws IOException ;

}
