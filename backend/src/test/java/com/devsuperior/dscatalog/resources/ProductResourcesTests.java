package com.devsuperior.dscatalog.resources;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc // tese web qnd carrega o contexto
public class ProductResourcesTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService service;

	@Autowired
	private TokenUtil tokenUtil;

	// Usado para converter para json
	@Autowired
	private ObjectMapper mapper;

	private PageImpl<ProductDTO> page;

	private ProductDTO productDTO;

	private Long validId;
	private Long invalidId;
	private Long dependentId;
	private String username;
	private String password;

	@BeforeEach
	public void setUp() throws Exception {
		validId = 1L;
		invalidId = 2L;
		dependentId = 3L;
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));
		username = "maria@gmail.com";
		password = "123456";

		Mockito.when(service.findAllPaged(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
				.thenReturn(page);

		Mockito.when(service.findById(validId)).thenReturn(productDTO);
		Mockito.when(service.findById(invalidId)).thenThrow(ResourceNotFoundException.class);

		Mockito.when((service.update(ArgumentMatchers.eq(validId), ArgumentMatchers.any()))).thenReturn(productDTO);
		Mockito.when((service.update(ArgumentMatchers.eq(invalidId), ArgumentMatchers.any())))
				.thenThrow(ResourceNotFoundException.class);

		Mockito.doNothing().when(service).delete(validId);
		Mockito.doThrow(ResourceNotFoundException.class).when(service).delete(invalidId);
		Mockito.doThrow(DatabaseException.class).when(service).delete(dependentId);

		Mockito.when(service.insert(ArgumentMatchers.any())).thenReturn(productDTO);

	}

	@Test
	public void insertShouldReturnProductDTO() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		String jsonBody = mapper.writeValueAsString(productDTO);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/products").content(jsonBody)
				.header("Authorization", "Bearer " + accessToken).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isCreated());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.price").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists());
	}

	@Test
	public void deleteShouldReturnNothingWhenExistingId() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", validId)
				.header("Authorization", "Bearer " + accessToken));
		result.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", invalidId)
				.header("Authorization", "Bearer " + accessToken));
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void deleteShouldReturnBadRequestWhenDependentId() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}", dependentId)
				.header("Authorization", "Bearer " + accessToken));
		result.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		// Converter objeto Java para JSON
		String jsonBody = mapper.writeValueAsString(productDTO);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", validId).content(jsonBody)
				.header("Authorization", "Bearer " + accessToken).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.description").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.price").exists());
	}

	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		String jsonBody = mapper.writeValueAsString(productDTO);
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", invalidId).content(jsonBody)
				.header("Authorization", "Bearer " + accessToken).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isNotFound());

	}

	@Test
	public void findAllShouldReturnPage() throws Exception {
		// perfom -> faz req. MockMvcRequestBuilders-> endpoint da req
		// MockMvcResultMatchers -> codigo esperado que retorne

		// First call the ENDPOINT
		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.get("/products").accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void findByIdShouldReturnProductDTOWhenIdIsValid() throws Exception {
		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.get("/products/{id}", validId).accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		// $ Acessa o objeto da resposta
		// No corpo da resposta precisa existir um campo de id
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.description").exists());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.price").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {

		ResultActions result = mockMvc
				.perform(MockMvcRequestBuilders.get("/products/{id}", invalidId).accept(MediaType.APPLICATION_JSON));
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
		// $ Acessa o objeto da resposta
		// No corpo da resposta precisa existir um campo de id
	}

}
