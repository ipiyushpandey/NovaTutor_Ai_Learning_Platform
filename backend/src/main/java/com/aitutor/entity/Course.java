package com.aitutor.entity;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor
public class Course { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(nullable=false) String title; String description; String level; String icon; Integer totalLessons=0; @Column(nullable=false, columnDefinition="boolean default true") boolean published=true; public Course(String t,String d,String l,String i){title=t;description=d;level=l;icon=i;} }
