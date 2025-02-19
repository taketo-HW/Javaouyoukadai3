package com.example.dockerapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Map;

@RestController
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Hello, Docker World!";
    }

    @GetMapping("/api/hoge")
    public String sayHoge() {
        return "hogehogehoge";
    }

    @GetMapping("/api/check-db")
    public String checkDbConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class); // MySQLへの接続確認
            return "Database connection is successful!";
        } catch (Exception e) {
            return "Database connection failed!";
        }
    }

    // ユーザー情報取得
    @GetMapping("/users/{user_id}")
    public ResponseEntity<?> getUserById(@PathVariable Long user_id) {
        try {
            String sql = "SELECT id, name, email FROM demo.users WHERE id = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, user_id);
            return ResponseEntity.ok(user);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    // ユーザー新規作成 (POST)
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");

            if (name == null || email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name and email are required");
            }

            String sql = "INSERT INTO demo.users (name, email) VALUES (?, ?)";
            jdbcTemplate.update(sql, name, email);

            String selectSql = "SELECT id FROM demo.users WHERE name = ? AND email = ? ORDER BY id DESC LIMIT 1";
            Long newUserId = jdbcTemplate.queryForObject(selectSql, Long.class, name, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", newUserId,
                    "name", name,
                    "email", email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
        }
    }

    // ユーザー情報更新 (PUT)
    @PutMapping("/users/{user_id}")
    public ResponseEntity<?> updateUser(@PathVariable Long user_id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");

            if (name == null || email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name and email are required");
            }

            String checkSql = "SELECT COUNT(*) FROM demo.users WHERE id = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, user_id);

            if (count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            String sql = "UPDATE demo.users SET name = ?, email = ? WHERE id = ?";
            jdbcTemplate.update(sql, name, email, user_id);

            return ResponseEntity.ok(Map.of(
                    "id", user_id,
                    "name", name,
                    "email", email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user");
        }
    }

    // ユーザー削除 (DELETE)
    @DeleteMapping("/users/{user_id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long user_id) {
        try {
            String checkSql = "SELECT COUNT(*) FROM demo.users WHERE id = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, user_id);

            if (count == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            String sql = "DELETE FROM demo.users WHERE id = ?";
            jdbcTemplate.update(sql, user_id);

            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }
}
