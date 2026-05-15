package com.tripmate.post;

import com.tripmate.common.BaseEntity;
import com.tripmate.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "companion_posts")
public class CompanionPost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    private User author;

    @Column(name = "city", nullable=false, length=80)
    private String cityCode;

    @Column(name = "start_date", nullable=false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(nullable=false, length=30)
    private String timeSlot;

    @ElementCollection(targetClass = CompanionPurpose.class)
    @CollectionTable(name = "post_purposes", joinColumns = @JoinColumn(name = "post_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 40)
    private List<CompanionPurpose> purposes = new ArrayList<>();

    @Column(nullable=false)
    private int maxParticipants;

    @Column(length=40)
    private String genderPreference;

    @ElementCollection
    @CollectionTable(name = "post_age_preferences", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "age_range", length = 20)
    private List<String> agePreferences = new ArrayList<>();

    @Column(nullable=false, length=100)
    private String title;

    @Column(nullable=false, length=3000)
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_travel_styles", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "style")
    private List<String> travelStyles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PostStatus status = PostStatus.OPEN;

    protected CompanionPost() {}

    public CompanionPost(User author, String cityCode, LocalDate startDate, LocalDate endDate, String timeSlot,
                         List<CompanionPurpose> purposes,
                         int maxParticipants, String genderPreference, List<String> agePreferences,
                         String title, String content, List<String> travelStyles) {
        this.author = author;
        this.cityCode = cityCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timeSlot = timeSlot;
        if (purposes != null) this.purposes.addAll(purposes);
        this.maxParticipants = maxParticipants;
        this.genderPreference = genderPreference;
        if (agePreferences != null) this.agePreferences.addAll(agePreferences);
        this.title = title;
        this.content = content;
        if (travelStyles != null) this.travelStyles.addAll(travelStyles);
    }
    public void close() {
        this.status = PostStatus.CLOSED;
    }
}
