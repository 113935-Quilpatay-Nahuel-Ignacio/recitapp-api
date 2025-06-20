# 🖼️ Guía del Sistema de Imágenes - RecitApp

## 📋 Cobertura del Sistema
Este sistema maneja imágenes para:
- **Eventos**: Flyers promocionales y mapas de secciones
- **Artistas**: Fotos de perfil/avatar  
- **Recintos**: Imágenes del lugar/instalaciones

## 🏗️ Arquitectura del Sistema

### Backend (Spring Boot)
```
📁 uploads/
├── 📁 event-flyers/         # Flyers promocionales de eventos
├── 📁 event-sections/       # Mapas de secciones para compra
├── 📁 artist-profiles/      # Fotos de perfil de artistas
└── 📁 venue-images/         # Imágenes de recintos
```

### Endpoints Disponibles
- `POST /api/files/upload/event-flyer` - Subir flyer de evento
- `POST /api/files/upload/event-sections` - Subir mapa de secciones
- `POST /api/files/upload/artist-profile` - Subir foto de artista
- `POST /api/files/upload/venue-image` - Subir imagen de recinto
- `POST /api/files/validate-image` - Validar imagen sin subir
- `DELETE /api/files/delete` - Eliminar archivo

### Validaciones de Seguridad
- **Formatos permitidos**: JPG, PNG, WebP, GIF
- **Tamaño máximo**: 10MB por archivo
- **Dimensiones máximas**: 2000x2000 píxeles
- **Validación MIME**: Verificación real del contenido
- **Nombres únicos**: Timestamp + UUID para evitar conflictos

## 🎯 Funcionalidades por Módulo

### 📅 Eventos
**Campos de imagen:**
- `flyerImage` - Imagen promocional del evento
- `sectionsImage` - Mapa/diagrama de secciones del recinto

**Funcionalidades:**
- Subida drag & drop con vista previa
- Reemplaza URLs manuales por sistema de archivos
- Vista expandida del mapa durante compra de tickets
- Compatibilidad con URLs existentes

### 🎤 Artistas  
**Campos de imagen:**
- `profileImage` - Foto de perfil/avatar del artista

**Funcionalidades:**
- Integración en formulario de creación/edición
- Vista previa inmediata al seleccionar archivo
- Compatibilidad con URLs existentes de fotos
- Manejo automático de errores de carga

### 🏟️ Recintos
**Campos de imagen:**
- `image` - Imagen principal del recinto

**Funcionalidades:**
- Subida en formulario de configuración de recinto
- Ayuda visual para identificar instalaciones
- Vista previa responsive
- Soporte para fotos exteriores e interiores

## 💻 Frontend (Angular)

### Componente Reutilizable
`FileUploadComponent` soporta las categorías:
- `event-flyer`
- `event-sections` 
- `artist-profile`
- `venue-image`

### Características UX
- **Drag & Drop**: Arrastra archivos directamente
- **Vista previa**: Imagen mostrada inmediatamente
- **Progreso**: Barra de carga en tiempo real
- **Validación**: Mensajes claros de error
- **Responsive**: Adapta a dispositivos móviles

## 🔒 Seguridad y Permisos

### Roles Requeridos
- **Eventos**: `ADMIN` o `REGISTRADOR_EVENTO`
- **Artistas**: `ADMIN`
- **Recintos**: `ADMIN`

### Medidas de Seguridad
- Validación de tipos MIME reales
- Prevención de archivos ejecutables
- Límites de tamaño estrictos
- Nombres de archivo sanitizados

## 📈 Guías de Uso

### Para Administradores

#### Crear Evento con Imágenes
1. Ir a "Eventos" → "Crear Nuevo"
2. Completar información básica
3. **Flyer del Evento**: Arrastrar imagen promocional
4. **Mapa de Secciones**: Subir diagrama del recinto
5. Guardar evento

#### Gestionar Artista
1. Ir a "Artistas" → "Crear/Editar"
2. Completar datos básicos
3. **Imagen de Perfil**: Subir foto del artista
4. Configurar redes sociales
5. Guardar perfil

#### Configurar Recinto  
1. Ir a "Recintos" → "Crear/Editar"
2. Datos de ubicación y capacidad
3. **Imagen del Recinto**: Foto representativa
4. Configurar secciones
5. Guardar configuración

### Para Compradores

#### Compra de Tickets con Mapa Visual
1. Seleccionar evento en el calendario
2. **Ver mapa de secciones**: Imagen clara del recinto
3. **Expandir imagen**: Click para vista completa
4. Elegir sección basado en ubicación visual
5. Proceder con la compra

## 🛠️ Configuración Técnica

### Variables de Entorno
```properties
# application.properties
app.file.upload-dir=uploads
app.file.base-url=http://localhost:8080
```

### Estructura de Directorios
```bash
# Backend - crear directorios
mkdir -p uploads/event-flyers
mkdir -p uploads/event-sections  
mkdir -p uploads/artist-profiles
mkdir -p uploads/venue-images
```

### Dependencias Maven
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-fileupload2-core</artifactId>
    <version>2.0.0-M2</version>
</dependency>
<dependency>
    <groupId>com.github.jai-imageio</groupId>
    <artifactId>jai-imageio-core</artifactId>
    <version>1.4.0</version>
</dependency>
```

## 🚀 Migración y Compatibilidad

### URLs Existentes
✅ **Totalmente compatibles** - No se requieren cambios en datos existentes

### Proceso de Migración
1. URLs existentes siguen funcionando
2. Nuevas imágenes usan sistema de archivos
3. Migración gradual opcional hacia archivos locales
4. Sin downtime durante implementación

## 🔧 Troubleshooting

### Problemas Comunes

**Error: "Archivo demasiado grande"**
- Verificar límite de 10MB
- Comprimir imagen si es necesario

**Error: "Formato no soportado"**
- Usar JPG, PNG, WebP o GIF únicamente
- Verificar que no sea un archivo corrupto

**Error: "Permisos insuficientes"**
- Verificar rol de usuario (ADMIN requerido)
- Contactar administrador del sistema

**Imagen no se muestra**
- Verificar conectividad al servidor
- Comprobar que el archivo existe en disco
- Revisar configuración de URLs base

### Logs para Debugging
```bash
# Buscar errores de subida
grep "Error al subir" logs/application.log

# Verificar archivos guardados  
grep "Archivo guardado" logs/application.log
```

## 🔮 Roadmap Futuro

### Próximas Mejoras
- [ ] **CDN Integration**: AWS S3/CloudFront para mejor rendimiento
- [ ] **Optimización automática**: Redimensionar imágenes al subir
- [ ] **Múltiples formatos**: Generar WebP automáticamente
- [ ] **Galería de imágenes**: Múltiples fotos por recinto
- [ ] **Watermarks**: Marca de agua automática
- [ ] **Analytics**: Métricas de uso de imágenes

### Escalabilidad
- Preparado para migración a almacenamiento en la nube
- Arquitectura modular permite cambios de backend
- Frontend desacoplado del sistema de almacenamiento

---

## 📞 Soporte

Para soporte técnico o consultas sobre el sistema de imágenes:
- Revisar logs del servidor para errores específicos
- Verificar permisos de directorio `uploads/`
- Confirmar configuración de `application.properties`

**¡Sistema listo para producción!** 🎉 