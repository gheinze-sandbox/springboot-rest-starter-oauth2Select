package bike.asi.configmgmt.controller;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ParentReference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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

    @Autowired Drive googleDrive;

    @RequestMapping(method = RequestMethod.POST)
    public StatusMessage handleFileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            return new StatusMessage(false, "Upload failed => the file was empty");
        }

        String googleDriveFileId;

        try {
            googleDriveFileId = uploadTextFile(multipartFile);
        } catch (IOException | IllegalStateException ex) {
            return new StatusMessage(false, "Upload failed => " + ex.getMessage());
        }

        return new StatusMessage(true, "https://drive.google.com/open?id=" + googleDriveFileId);

    }


    private static final List<ParentReference> PARENT_REFERENCES = Lists.newArrayList(
            new ParentReference().setId("0B4RyV1qO0-W6NUtiejAyUGhCSWc"), // ASI
            new ParentReference().setId("0B4RyV1qO0-W6aUhOR2dCc1o0b2c") // eblox
    );


    private String uploadTextFile(MultipartFile multipartFile) throws IOException {

        com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
        body.setTitle(multipartFile.getOriginalFilename());
        body.setParents(PARENT_REFERENCES);
        body.setDescription("EBlox configuration loaded via ASI Configuration Management");
        body.setMimeType(multipartFile.getContentType());

        com.google.api.services.drive.model.File googleDriveFile;

        try (InputStream uploadFileStream = multipartFile.getInputStream()) {
            InputStreamContent mediaContent = new InputStreamContent("text/plain", uploadFileStream);
            googleDriveFile = googleDrive.files().insert(body, mediaContent).execute();
        }

        return googleDriveFile.getId();

    }

}
