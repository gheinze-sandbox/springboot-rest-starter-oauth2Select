package bike.asi.configmgmt.controller;

import bike.asi.configmgmt.util.cloudstore.CloudStore;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 *
 * @author gheinze
 */
@RestController
@RequestMapping(value="/api/upload/eblox")
public class EbloxUploadController {

    @Autowired CloudStore cloudStore;

    @RequestMapping(method = RequestMethod.POST)
    public StatusMessage handleFileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            return new StatusMessage(false, "Upload failed => the file was empty");
        }

        String cloudFileLocation;

        try {
            cloudFileLocation = cloudStore.transferToCloud(multipartFile);
        } catch (IOException ex) {
            return new StatusMessage(false, "Upload failed => " + ex.getMessage());
        }

        return new StatusMessage(true, cloudFileLocation);

    }


}
