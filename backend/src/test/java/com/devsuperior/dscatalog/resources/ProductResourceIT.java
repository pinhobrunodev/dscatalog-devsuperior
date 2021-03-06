package com.devsuperior.dscatalog.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductResourceIT {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper mapper;

    private Long validId;
    private Long invalidId;
    private Long countTotalProducts;
    private String username;
    private String password;

    @BeforeEach
    public void setUp() throws Exception {
        validId = 1L;
        invalidId = 1000L;
        countTotalProducts = 25L;
        username = "maria@gmail.com";
        password = "123456";
    }

    @Test
    public void findAllShouldReturnSortedPageWhenSortByName() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/products?page=0&size=12&sort=name,asc")
                .accept(MediaType.APPLICATION_JSON));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(countTotalProducts));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.content").exists());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Macbook Pro"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value("PC Gamer"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name").value("PC Gamer Alfa"));
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

    	
        ProductDTO productDTO = Factory.createProductDTO();

        //Converter objeto Java para JSON
        String jsonBody = mapper.writeValueAsString(productDTO);

        //Salvar o nome anterior
        String expectedName = productDTO.getName();
        String expectedDescription = productDTO.getDescription();

        ResultActions result = mockMvc
                .perform(MockMvcRequestBuilders.put("/products/{id}", validId)
                		.header("Authorization", "Bearer " + accessToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(validId));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expectedName));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.description").value(expectedDescription));
        result.andExpect(MockMvcResultMatchers.jsonPath("$.price").exists());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

    	String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
    	
        ProductDTO productDTO = Factory.createProductDTO();

        //Converter objeto Java para JSON
        String jsonBody = mapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc
                .perform(MockMvcRequestBuilders.put("/products/{id}", invalidId)
                		.header("Authorization", "Bearer " + accessToken)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
