package ch.unil.doplab;

import java.util.UUID;

public class Company {
    private UUID id;
    private UUID ownerEmployerId;
    private String name;
    private String location;
    private String description;

    private Company() {}

    private Company(UUID id, UUID ownerEmployerId, String name, String location, String description) {
        this.id = id;
        this.ownerEmployerId = ownerEmployerId;
        this.name = name;
        this.location = location;
        this.description = description;
    }

    public Company(UUID ownerEmployerId, String name, String location, String description) {
        this(null, ownerEmployerId, name, location, description);
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerEmployerId() {return ownerEmployerId; }
    public void setOwnerEmployerId(UUID id) { this.ownerEmployerId = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // methods?


}
