# Personal Photo Assistant

A Simple personal photo assistant. This can access personal SmugMug repository via API to answer questions about photos.

## Description

This repo contains a simple photo assistant that can access SmugMug repository via API to answer questions about photos.  
This application is intended to demonstrate the integration of RAG (Retrieval Augmented Generation), tools (AI Tools) and SmugMug Photos APIs with Spring AI and Ollama.  

## Getting Started

### Dependencies
* Personal account on SmugMug
* Spring AI
* Java SmugMug Client
* PGVector Vector Database
* Ollama
* llama 3.1 model
* llava model

### How do I get set up? ###
* First, need to have a SmugMug account and API key and Access token, and it's corresponding secret.
* Get SmugMug Java Client
* Install Ollama from https://ollama.com
* Install PGVector  

Simplest way to run PGVector

```commandline
To start PGVector container
 $ docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres ankane/pgvector
```
To persist data, use docker volume. For example consider following docker-compose.yaml
```yaml
services:
  db:
    image: ankane/pgvector
    restart: always
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: mydb
    ports:
      - "5432:5432"
    volumes:
       - ./data:/var/lib/postgresql/data
```
```commandline
Start Docker using docker compose
$ docker compose up

```
### Configuration
* Get JavaSmugMugClient from https://github.com/konjarla/JavaSmugMugClient
* Include JavaSmugMugClient in maven pom.xml
```xml
<dependency>
  <groupId>net.konjarla.smugmug</groupId>
  <artifactId>smugmugclient</artifactId>
  <scope>system</scope>
  <version>1.0-SNAPSHOT</version>
  <systemPath>PATH_TO_JAR_FILE</systemPath>
</dependency> 
```
* Refer to pom.xml

* Add SmugMug API key and access token and secret to application.properties via environment variables.
* Then, add the following properties to application.properties
``` properties
smugmug.consumer.key=${SM_CONSUMER_KEY}
smugmug.consumer.secret=${SM_CONSUMER_SECRET}
smugmug.access.token=${SM_ACCESS_TOKEN}
smugmug.access.token.secret=${SM_ACCESS_TOKEN_SECRET}
```
* This application disables auto configuration of Ollama and PGVector for Spring AI. 
* To attain granularity of control, you can manually configure Ollama and PGVector using the following configuration properties:
``` properties
## For Vector Store/DB
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

photo.caption.vector.store=photo_caption_vector_store
photo.caption.vector.store.dim=1024
photo.vector.store.top_k=10
photo.vector.store.similarity_threshold=0.2
photo.vector.store.similarity_score_threshold=0.5
photo.chat.system.prompt.template.file=classpath:system_prompt_photo_chat_1.st

# For Ollama
ollama.host=http://localhost:11434
ollama.chat.model=llama3.1:latest

# Set to default value
spring.ai.ollama.chat.options.num-ctx=2048

ollama.chat.options.temperature=0.3
ollama.photo.chat.model=llava-llama3
ollama.photo.chat.options.temperature=0.3
```
* Tune the spring.ai.ollama.chat.options.num-ctx to adjust the context window used to generate the next token.  
  If the machine that Ollama running on has adequate resources (memory and GPU/CPU), you can increase the context window to improve the quality of the generated text.  
  **NOTE:** I have set the context value to 32678 on my development machine Macbook Pro with M4Pro Chip and 48GB RAM.
### Executing program
* Build the project. Typically,
```commandline
$ mvn clean install
```
* Run the application. It runs on port 7070.
```commandline
$ mvn spring-boot:run 
```
* Or run the jar file
```commandline
$ java -jar target/smagent-0.0.1-SNAPSHOT.jar
```
### Setup for RAG
* Prepare the vector database for RAG.
* Index albums data to search for albums based on description using "indexalbums" endpoint.  
  **NOTE:** This will iterate through entire SmugMug library and index all the albums.
```commandline
$ curl 'http://localhost:7070/photo/indexalbums'
```
* Prepare an album for RAG. This will fetch all the photos in the album and index their captions to the vector database.  
  This facilitates RAG based semantic search for photos.
```commandline
$ curl 'http://localhost:7070/photo/index/<SmugMug albumkey>'
```
### Usage
* Use /photo/chat endpoint to get chat responses from the LLM.
```commandline
curl -X POST -H 'Accept: application/json' -H 'Content-Type: application/json' -i http://localhost:7070/photo/chat --data '{"type": "text", "content": "find photos of lake surrounded with mountains", "sender": "user"}'
```
* Also, React bassed UI application (smugmugchat) is available.
### LLM Tools
* All LLM tools are available in PhotoTools class. We are using methods as tools.
* The following LLM tools are available:
```text
@Tool(description = "For a given description, find the most similar photos.")
    public List<SMAgentRecord.PhotoSearchResponse> findPicturesTool(String query)

@Tool(description = "For given description and album, find the most similar photos.")
    public List<SMAgentRecord.PhotoSearchResponse> findPicturesInAlbumTool(String query, String album)

@Tool(description = "Retrieve and analyze metadata (e.g., album, caption, location, aperture, exposure, timestamp etc.) for a specified photo ID.")
    public SMAgentRecord.PhotoSearchResponse getPhotoInformationByIdTool(String id)
    
@Tool(description = "Finds albums that match a given description or query.")
    public List<Document> findAlbumsSymantically(String query)
    
@Tool(description = "Searches for albums by name and retrieves detailed information about each matching album.")
    public List<SMAlbum> findAlbumsByNameOrKey(String name)
    
@Tool(description = "Retrieves detailed information about an album, using either the album's unique key or its name.")
    public SMAlbum getAlbumInformationTool(String albumNameOrId)
    
@Tool(description = "Analyzes the visual content of a photo using its ID and answers questions about what is depicted, excluding technical metadata. This tools is very useful when photo ID is available in the context.", returnDirect = true)
    public String analyzePhotoContent(String id, String query)
    
@Tool(description = "Analyzes the visual content of a photo using its WebUri and answers questions about what is depicted, excluding technical metadata. This tools is very useful when photo ID is NOT available and WebUri is available in the context.", returnDirect = true)
    public String analyzePhotoContentByWebUri(String webUri, String query)                 
```
## Author
**Srikanth Konjarla**

## Version History

* 0.1
    * Initial Release

## License

This project is licensed under the 

* [MIT License](https://mit-license.org/)

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.1/maven-plugin/build-image.html)
* [Spring AI](https://docs.spring.io/spring-ai/reference/index.html)
* [Ollama](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)
* [PGvector Vector Database](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.1/reference/web/servlet.html)


