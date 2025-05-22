package org.example;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * {@code CustomUserDetails} extends Spring Security's {@link User} class
 * to include additional user profile attributes such as full name and location.
 */
public class CustomUserDetails extends User {

    /** The full name of the user. */
    private final String fullName;

    /** The location associated with the user (e.g., department or facility). */
    private final String location;

    /** The encoded password of the user, stored for explicit access. */
    private final String password;

    /**
     * Constructs a new {@code CustomUserDetails} object.
     *
     * @param username   the user's login identifier
     * @param password   the encoded password
     * @param authorities the collection of granted roles/authorities
     * @param fullName   the user's full display name
     * @param location   the user's associated location
     */
    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String fullName, String location) {
        super(username, password, authorities);
        this.password = password;
        this.fullName = fullName;
        this.location = location;
    }

    /**
     * Returns the user's full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the user's associated location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the user's encoded password.
     *
     * @return the encoded password
     */
    @Override
    public String getPassword() {
        return password;
    }
}
