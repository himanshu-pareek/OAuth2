package dev.javarush.oauth2.resource_server.contact;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("contacts")
public class ContactController {

    @GetMapping
    public String getAll() {
        return "Hello, world 👋👋👋\n";
    }

    @PostMapping
    public Map<String, Object> create(
            @RequestBody Map<String, Object> contact
    ) {
        contact.put("created", true);
        return contact;
    }

}
