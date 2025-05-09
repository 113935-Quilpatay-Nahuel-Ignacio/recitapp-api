package com.recitapp.recitapp_api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando una entidad no se encuentra en la base de datos
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

  /**
   * Construye una nueva excepción EntityNotFoundException con el mensaje de error especificado
   *
   * @param message El mensaje de error detallado
   */
  public EntityNotFoundException(String message) {
    super(message);
  }

  /**
   * Construye una nueva excepción EntityNotFoundException para una entidad y ID específicos
   *
   * @param entityName Nombre de la entidad (por ejemplo, "Usuario", "Evento")
   * @param id ID de la entidad que no se encontró
   * @return Una excepción con un mensaje formateado
   */
  public static EntityNotFoundException create(String entityName, Long id) {
    return new EntityNotFoundException(
            String.format("No se encontró %s con ID: %d", entityName, id));
  }

  /**
   * Construye una nueva excepción EntityNotFoundException para una entidad y nombre específicos
   *
   * @param entityName Nombre de la entidad (por ejemplo, "Usuario", "Evento")
   * @param name Nombre o identificador de la entidad que no se encontró
   * @return Una excepción con un mensaje formateado
   */
  public static EntityNotFoundException createByName(String entityName, String name) {
    return new EntityNotFoundException(
            String.format("No se encontró %s con nombre: %s", entityName, name));
  }
}