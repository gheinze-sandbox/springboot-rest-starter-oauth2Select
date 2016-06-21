package bike.asi.configmgmt.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author gheinze
 */
@RestController
@RequestMapping(value="/api/hello")
public class HelloController {

    @RequestMapping(value="/{user}", method=RequestMethod.GET)
    public String getUser(@PathVariable String user) {
        return "Hello there " + user;
    }

}
