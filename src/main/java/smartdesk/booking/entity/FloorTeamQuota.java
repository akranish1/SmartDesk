package smartdesk.booking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "floor_team_quotas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_quota_floor_team",
                        columnNames = {"floor_id", "team_id"}
                )
        }
)
public class FloorTeamQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "floor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_quota_floor")
    )
    private Floor floor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "team_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_quota_team")
    )
    private Team team;

    @Column(name = "max_concurrent_bookings", nullable = false)
    private Integer maxConcurrentBookings;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FloorTeamQuota() {
    }

    public FloorTeamQuota(
            Floor floor,
            Team team,
            Integer maxConcurrentBookings
    ) {
        this.floor = floor;
        this.team = team;
        this.maxConcurrentBookings = maxConcurrentBookings;
    }

    public Long getId() {
        return id;
    }

    public Floor getFloor() {
        return floor;
    }

    public Team getTeam() {
        return team;
    }

    public Integer getMaxConcurrentBookings() {
        return maxConcurrentBookings;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setMaxConcurrentBookings(Integer maxConcurrentBookings) {
        this.maxConcurrentBookings = maxConcurrentBookings;
    }
}