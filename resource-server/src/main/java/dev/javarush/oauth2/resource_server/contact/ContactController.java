package dev.javarush.oauth2.resource_server.contact;

import dev.javarush.oauth2.resource_server.security.AccessDeniedException;
import dev.javarush.oauth2.resource_server.security.UserContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("contacts")
public class ContactController {

    List<Contact> contacts;

    public ContactController() {
        this.contacts = new ArrayList<>();
        contacts.add(new Contact(1, "user1", "John", "john@email.com"));
        contacts.add(new Contact(2, "user1", "Peter", "peter@email.com"));
        contacts.add(new Contact(3, "user3", "Smith", "smith@email.com"));
        contacts.add(new Contact(4, "user3", "Raj", "raj@email.com"));
    }

    @GetMapping
    public Collection<Contact> getAll() {
        String currentUserId = UserContextHolder.getUserId();
        return contacts.stream()
                .filter(contact -> Objects.equals(contact.userId(), currentUserId))
                .toList();
    }

    @GetMapping("{id}")
    public Contact getOne (@PathVariable("id") int id) {
        String currentUserId = UserContextHolder.getUserId();
        Optional<Contact> existing = contacts.stream()
                .filter(contact -> id == contact.id())
                .findFirst();
        if (existing.isEmpty()) {
            return null;
        }
        if (!Objects.equals(existing.get().userId(), currentUserId)) {
            throw new AccessDeniedException(
                    "access_denied",
                    "You can not access contact with id " + existing.get().id(),
                    List.of("contact.read")
            );
        }
        return existing.get();
    }

    @PostMapping
    public Contact create(@RequestBody Contact contact) {
        String currentUserId = UserContextHolder.getUserId();
        Contact created = new Contact(
                contacts.size() + 1,
                currentUserId,
                contact.name(),
                contact.email()
        );
        contacts.add(created);
        return created;
    }

    @PutMapping("{id}")
    public Contact update(@PathVariable("id") int id, @RequestBody Contact contact) {
        String currentUserId = UserContextHolder.getUserId();

        this.contacts.stream()
                .filter(c -> c.id() == id)
                .filter(c -> Objects.equals(c.userId(), currentUserId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException(
                        "access_denied",
                        "You can not update contact with id " + id,
                        List.of("contact.read", "contact.write")
                ));

        var updated = new Contact(id, currentUserId, contact.name(), contact.email());
        this.contacts = this.contacts.stream()
                .map(c -> {
                    if (c.id() == id) {
                        return updated;
                    }
                    return c;
                })
                .toList();
        return updated;
    }
}
