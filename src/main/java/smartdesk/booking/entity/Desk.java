package smartdesk.booking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "desks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_desk_number_per_floor",
                        columnNames = {"floor_id", "desk_number"}
                )
        }
)
public class Desk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "floor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_desk_floor")
    )
    private Floor floor;

    @Column(
            name = "desk_number",
            nullable = false,
            length = 50
    )
    private String deskNumber;

    @Column(
            name = "x_coordinate",
            nullable = false
    )
    private Integer xCoordinate;

    @Column(
            name = "y_coordinate",
            nullable = false
    )
    private Integer yCoordinate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "desk_type",
            nullable = false,
            length = 20
    )
    private DeskType deskType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private DeskStatus status = DeskStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_user_id",
            foreignKey = @ForeignKey(name = "fk_desk_assigned_user")
    )
    private User assignedUser;

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

    protected Desk() {
    }

    public Desk(
            Floor floor,
            String deskNumber,
            Integer xCoordinate,
            Integer yCoordinate,
            DeskType deskType,
            User assignedUser
    ) {
        this.floor = floor;
        this.deskNumber = deskNumber;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.deskType = deskType;
        this.assignedUser = assignedUser;
        this.status = DeskStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Floor getFloor() {
        return floor;
    }

    public String getDeskNumber() {
        return deskNumber;
    }

    public Integer getXCoordinate() {
        return xCoordinate;
    }

    public Integer getYCoordinate() {
        return yCoordinate;
    }

    public DeskType getDeskType() {
        return deskType;
    }

    public DeskStatus getStatus() {
        return status;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(DeskStatus status) {
        this.status = status;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }
}