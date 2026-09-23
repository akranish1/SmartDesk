package smartdesk.booking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "floors",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_floor_building_number",
                        columnNames = {"building", "floor_number"}
                )
        }
)
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "building",
            nullable = false,
            length = 100
    )
    private String building;

    @Column(
            name = "floor_number",
            nullable = false
    )
    private Integer floorNumber;

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "timezone",
            nullable = false,
            length = 50
    )
    private String timezone;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected Floor() {
    }

    public Floor(
            String building,
            Integer floorNumber,
            String name,
            String timezone
    ) {
        this.building = building;
        this.floorNumber = floorNumber;
        this.name = name;
        this.timezone = timezone;
    }

    public Long getId() {
        return id;
    }

    public String getBuilding() {
        return building;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public String getName() {
        return name;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}