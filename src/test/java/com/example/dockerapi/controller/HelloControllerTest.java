package com.example.dockerapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.HashMap;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testSayHello() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Docker World!"));
    }

    @Test
    public void testSayHoge() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/hoge"))
                .andExpect(status().isOk())
                .andExpect(content().string("hogehogehoge"));
    }

    @Test
    public void testCheckDbConnectionSuccess() throws Exception {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class)))
                .thenReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/check-db"))
                .andExpect(status().isOk())
                .andExpect(content().string("Database connection is successful!"));
    }

    @Test
    public void testCheckDbConnectionFailure() throws Exception {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class)))
                .thenThrow(new RuntimeException());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/check-db"))
                .andExpect(status().isOk())
                .andExpect(content().string("Database connection failed!"));
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        Long userId = 1L;
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("name", "John Doe");
        user.put("email", "john@example.com");

        Mockito.when(jdbcTemplate.queryForMap(Mockito.anyString(), Mockito.any()))
                .thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        Mockito.when(jdbcTemplate.queryForMap(Mockito.anyString(), Mockito.any()))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException(1));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        String requestBody = "{ \"name\": \"Jane Doe\", \"email\": \"jane@example.com\" }";

        Mockito.when(jdbcTemplate.update(Mockito.anyString(), Mockito.any(Object[].class)))
                .thenReturn(1);
        Mockito.when(
                jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Long.class), Mockito.any(Object[].class)))
                .thenReturn(2L);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    public void testCreateUser_BadRequest() throws Exception {
        String requestBody = "{ \"name\": null, \"email\": null }";

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name and email are required"));
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        String requestBody = "{ \"name\": \"Updated Name\", \"email\": \"updated@example.com\" }";

        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class),
                Mockito.any(Object[].class)))
                .thenReturn(1);
        Mockito.when(jdbcTemplate.update(Mockito.anyString(), Mockito.any(Object[].class)))
                .thenReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")

                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    public void testUpdateUser_NotFound() throws Exception {
        String requestBody = "{ \"name\": \"Updated Name\", \"email\": \"updated@example.com\" }";

        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class),
                Mockito.any(Object[].class)))
                .thenReturn(0);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))

                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    public void testDeleteUser_Success() throws Exception {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class),
                Mockito.any(Object[].class)))
                .thenReturn(1);
        Mockito.when(jdbcTemplate.update(Mockito.anyString(), Mockito.any(Object[].class)))
                .thenReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(status().isOk())

                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    public void testDeleteUser_NotFound() throws Exception {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class),
                Mockito.any(Object[].class)))
                .thenReturn(0);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

}
