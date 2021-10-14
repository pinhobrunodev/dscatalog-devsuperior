package com.devsuperior.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;

// Implementa uma interface do BeansValidation e passamos como parametro a annotation criada e o tipo da classe que vai receber essa annotation
public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {
	
	
	// Guarda as informacoes da Requisicao, apartir dele consigo pegar o codigo q passei na req do Update (ID)
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(UserUpdateValid ann) {
	}

	
	// Este metodo do ConstraintValidator vai tester se o meu dto vai ser valido ou nao
	// Se retornar TRUE = SEM ERRO || retornar FALSE  = Pelo menos 1 erro
	@Override
	public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {
		
		// Pega um dicionario com os atributos de uma URL
		// Chave - Valor em HTTP = String
		@SuppressWarnings("unchecked")
		var uriVars = (Map<String,String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		long userId = Long.parseLong(uriVars.get("id")); // ''ID'' do endpoint  /{id}
		List<FieldMessage> list = new ArrayList<>();
		
		// Coloque aqui seus testes de validação, acrescentando objetos FieldMessage à lista
		
		User user = repository.findByEmail(dto.getEmail());
		// Fiz a busca no DB pelo Email , retornei um Usuario , 
		//se o ID DESSE USUARIO N FOR O ID DO CARA Q TO QUERENDO ATUALIZAR ( ESTAREI TENTANDO ATUALIZAR UM EMAIL DE UM USUARIO Q JA EXISTE)
		
		if (user != null && userId != user.getId()) {
			list.add(new FieldMessage("email","Email já existe"));
		}
		
		
		// Estou percorrendo a lista de fieldMessage e pegando cada erro encontrado e  inserindo na lista do BeansValidation
		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		// testa se a lista esta vazia , se a minha lista terminar vazia significa q nenhum teste deu erro e vice-versa
		return list.isEmpty();
	}
}

