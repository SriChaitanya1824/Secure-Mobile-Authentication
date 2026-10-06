package com.srichaitanya.secureauth.user
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
@Entity @Table(name="users") class User(@Id var id:UUID=UUID.randomUUID(), @Column(unique=true) var email:String="", @Column(name="password_hash") var passwordHash:String="", @Column(name="display_name") var displayName:String="", var active:Boolean=false, var locked:Boolean=false, @Column(name="created_at") var createdAt:Instant=Instant.now())
interface UserRepository:org.springframework.data.jpa.repository.JpaRepository<User,UUID> { fun findByEmail(email:String):User? }
