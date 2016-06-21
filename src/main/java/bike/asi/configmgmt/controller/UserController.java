package bike.asi.configmgmt.controller;

import java.util.Map;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author gheinze
 */
@RestController
public class UserController {

    @RequestMapping("/user")
    public String user(OAuth2Authentication auth) {
        Map details = (Map)auth.getUserAuthentication().getDetails();
        return (String)details.get("name");
    }


}
