# Repositorio de CRUD de Archivos con Spring y Amazon S3 🗃️🚀

Este repositorio contiene un **CRUD (Create, Read, Update, Delete)** básico para gestionar archivos almacenados en un **bucket de Amazon S3**. A continuación, se describen los endpoints disponibles:

## Endpoints del Controlador S3

1. **`GET /s3/listFiles`**: Obtiene la lista de todos los archivos almacenados en el bucket de S3.

2. **`GET /s3/download/{fileName}`**: Descarga un archivo específico por su nombre.

3. **`GET /s3/downloadAsBytes/{fileName}`**: Descarga un archivo como bytes (por ejemplo, una imagen).

4. **`POST /s3/upload`**: Sube un archivo al bucket de S3.

5. **`PUT /s3/rename/{oldFileName}/{newFileName}`**: Cambia el nombre de un archivo existente.

6. **`PUT /s3/update/{oldFileName}`**: Actualiza un archivo existente con un nuevo contenido.

7. **`DELETE /s3/delete/{fileName}`**: Elimina un archivo del bucket de S3.

Recuerda configurar tus credenciales de AWS como variables de entorno y ajustar la implementación de `IS3Service` para que se adapte a tus necesidades específicas. ¡Buena suerte con tu proyecto! 🙌🏼
