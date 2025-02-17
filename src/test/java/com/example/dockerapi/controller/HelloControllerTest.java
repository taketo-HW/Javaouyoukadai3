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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate; // ここを `@MockBean` → `@MockitoBean` に変更

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
}
