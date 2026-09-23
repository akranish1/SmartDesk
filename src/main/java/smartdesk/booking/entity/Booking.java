package smartdesk.booking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_booking_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "desk_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_booking_desk")
    )
    private Desk desk;

    @Column(
            name = "start_time",
            nullable = false
    )
    private Instant startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private BookingStatus status = BookingStatus.CONFIRMED;

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

    protected Booking() {
    }

    public Booking(
            User user,
            Desk desk,
            Instant startTime,
            Instant endTime
    ) {
        this.user = user;
        this.desk = desk;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = BookingStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Desk getDesk() {
        return desk;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}