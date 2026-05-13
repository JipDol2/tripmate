package com.tripmate.user;

import com.tripmate.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=120)
    private String email;

    @Column(nullable=false)
    private String password;

    @Column(nullable=false, length=40)
    private String nickname;

    @Column(length=20)
    private String ageRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Gender gender = Gender.PRIVATE;

    @Column(length=500)
    private String bio;

    @Column(length=500)
    private String profileImageUrl;

    @ElementCollection
    @CollectionTable(name = "user_travel_styles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "style")
    private List<String> travelStyles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "language")
    private List<String> languages = new ArrayList<>();

    protected User() {}

    public User(String email, String password, String nickname, String ageRange, Gender gender) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.ageRange = ageRange;
        this.gender = gender;
    }

    public void updateProfile(String nickname, String ageRange, Gender gender, String bio, String profileImageUrl,
                              List<String> travelStyles, List<String> languages) {
        this.nickname = nickname;
        this.ageRange = ageRange;
        this.gender = gender;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.travelStyles.clear();
        if (travelStyles != null) this.travelStyles.addAll(travelStyles);
        this.languages.clear();
        if (languages != null) this.languages.addAll(languages);
    }
}
