package com.azki.reservation.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@Table(name = "tbl_available_slots")
public class AvailableSlotsEntity extends BaseEntity<Long> {
    @Id
    @SequenceGenerator(name="seq_available_slots",sequenceName="seq_available_slots",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_available_slots")
    private Long id;

    @Column(name = "start_time",nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time",nullable = false)
    private LocalDateTime endTime;
    @Column(name = "is_reserved",nullable = false)
    private Boolean isReserved = false;
    @ManyToOne
    @JoinColumn(name = "reserved_by")
    private UserEntity reservedBy;
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Override
    public String toString() {
        return new StringJoiner(", ", AvailableSlotsEntity.class.getSimpleName() + "[", "]")
                .add("startTime=" + startTime)
                .add("endTime=" + endTime)
                .add("isReserved=" + isReserved)
                .add("reservedBy=" + (reservedBy == null ? "null" : reservedBy.toString()))
                .add("reservedAt=" + reservedAt)
                .toString();
    }
}
