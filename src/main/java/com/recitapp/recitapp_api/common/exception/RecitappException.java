package com.recitapp.recitapp_api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción general de la aplicación Recitapp
 * Usada para manejar errores de negocio específicos de la aplicación
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecitappException extends RuntimeException {

    /**
     * Construye una nueva excepción RecitappException con el mensaje de error especificado
     *
     * @param message El mensaje de error detallado
     */
    public RecitappException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción RecitappException con el mensaje de error y la causa
     *
     * @param message El mensaje de error detallado
     * @param cause La causa original de la excepción
     */
    public RecitappException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Excepción para indicar que no se puede realizar una operación porque el recurso ya existe
     *
     * @param entityName Nombre de la entidad (por ejemplo, "Usuario", "Evento")
     * @param identifier Identificador del recurso (nombre, ID, etc.)
     * @return Una excepción con un mensaje formateado
     */
    public static RecitappException alreadyExists(String entityName, String identifier) {
        return new RecitappException(
                String.format("Ya existe %s con %s", entityName, identifier));
    }

    /**
     * Excepción para indicar que no se puede realizar una operación en un recurso
     *
     * @param entityName Nombre de la entidad (por ejemplo, "Usuario", "Evento")
     * @param reason Razón por la que no se puede realizar la operación
     * @return Una excepción con un mensaje formateado
     */
    public static RecitappException operationNotAllowed(String entityName, String reason) {
        return new RecitappException(
                String.format("No se puede realizar la operación en %s: %s", entityName, reason));
    }
}