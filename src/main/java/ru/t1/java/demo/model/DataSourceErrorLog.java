package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "data_source_error_log")
public class DataSourceErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stackTraceText")
    private String stackTraceText;
    @Column(name = "stackTraceMessage")
    private String stackTraceMessage;
    @Column(name = "methodSignature")
    private String methodSignature;


}