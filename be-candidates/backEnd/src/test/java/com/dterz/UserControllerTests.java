package com.dterz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.dterz.dtos.UserDTO;
import com.dterz.model.User;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests extends BaseTests {

    @Test
    @WithMockUser("spring")
    public void testGetById() throws Exception {

        // Mocking the return value of findById on userRepository
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        this.mockMvc.perform(get("/api/user/1").accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pass").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fistName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.superAdmin").value(true))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    @WithMockUser("spring")
    public void testGetAll() throws Exception {

        List<User> users = new ArrayList<>();
        users.add((user));
        when(userRepository.findAll()).thenReturn(users);

        MvcResult result = this.mockMvc.perform(get("/api/user").accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pass").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].fistName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].surName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].comments").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].superAdmin").value(true))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(jsonResponse);
        assertEquals(users.size(), jsonArray.length());

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }

    @Test
    @WithMockUser("spring")
    public void testUpdate() throws Exception {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(Mockito.any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserDTO updatedUserDTO = new UserDTO();

        updatedUserDTO.setId(user.getId());
        updatedUserDTO.setUserName("spring_updated");
        updatedUserDTO.setPass(user.getPass());
        updatedUserDTO.setFistName("spring_firstname");
        updatedUserDTO.setSurName(user.getSurName());
        updatedUserDTO.setComments(user.getComments());
        updatedUserDTO.setSuperAdmin(false);

        this.mockMvc.perform(post("/api/user")
                .content(asJsonString(updatedUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value("spring_updated"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pass").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fistName").value("spring_firstname"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surName").value("spring"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.superAdmin").value(false))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));

    }

    @Test
    @WithMockUser("spring")
    public void testCreateAccount() throws Exception {

        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(userRepository.save(Mockito.any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserDTO newUserDTO = new UserDTO();

        newUserDTO.setId(2);
        newUserDTO.setUserName("spring2");
        newUserDTO.setPass("test");
        newUserDTO.setFistName("spring2");
        newUserDTO.setSurName("spring2");
        newUserDTO.setComments("");
        newUserDTO.setSuperAdmin(true);

        this.mockMvc.perform(post("/api/user")
                .content(asJsonString(newUserDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value("spring2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fistName").value("spring2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surName").value("spring2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comments").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.superAdmin").value(true))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userRepository, Mockito.times(1)).findById(2L);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }
}
