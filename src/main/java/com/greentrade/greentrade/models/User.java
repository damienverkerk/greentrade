package com.greentrade.greentrade.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  

    @Column(unique = true)
    private String email;

    private String password;  

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean verificationStatus;  

    @OneToOne
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)  
    @Builder.Default
    private List<Product> products = new ArrayList<>();  

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)  
    @Builder.Default
    private List<Message> sentMessages = new ArrayList<>();  

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)  
    @Builder.Default
    private List<Message> receivedMessages = new ArrayList<>();  

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return verificationStatus;
    }
}